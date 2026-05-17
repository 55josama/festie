import client, { reissueAccessToken } from './client'
import { unwrap, unwrapPageResponse } from '../lib/api'
import { useAuthStore } from '../store/authStore'
import type { Notification } from '../types'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim() || ''

const notificationBaseUrl = apiBaseUrl.replace(/\/$/, '')

function buildAuthHeaders() {
  const store = useAuthStore.getState()
  store.syncUserFromAccessToken()
  const { accessToken, user } = useAuthStore.getState()
  if (!accessToken || !user?.userId || !user?.role) {
    return null
  }

  return {
    Authorization: `Bearer ${accessToken}`,
    'X-User-Id': user.userId,
    'X-User-Role': user.role,
    Accept: 'text/event-stream',
  }
}

export const getNotifications = async (params: Record<string, any> = {}) => {
  const res = await client.get('/notification-service/v1/notifications', { params })
  return unwrapPageResponse<Notification>(res.data)
}

export const markAllNotificationsAsRead = async () => {
  const res = await client.patch('/notification-service/v1/notifications')
  return unwrap<Notification[]>(res.data)
}

export const deleteNotification = async (notificationId: string) => {
  const res = await client.delete(`/notification-service/v1/notifications/${notificationId}`)
  return unwrap<void>(res.data)
}

type StreamHandlers = {
  onNotification: (notification: Notification) => void
  onConnect?: () => void
  onError?: (error: Error) => void
}

async function openNotificationStream(headers: Record<string, string>, signal: AbortSignal, handlers: StreamHandlers) {
  const response = await fetch(`${notificationBaseUrl}/notification-service/v1/notifications/subscribe`, {
    method: 'GET',
    headers,
    signal,
  })

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('UNAUTHORIZED')
    }
    throw new Error(`notification stream failed: ${response.status}`)
  }

  if (!response.body) {
    throw new Error('notification stream body is missing')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let currentEvent = 'message'
  let dataLines: string[] = []

  const flush = () => {
    if (!dataLines.length && currentEvent === 'message') {
      return
    }

    const payload = dataLines.join('\n')
    if (currentEvent === 'connect') {
      handlers.onConnect?.()
    } else if (currentEvent === 'notification' && payload) {
      try {
        handlers.onNotification(JSON.parse(payload) as Notification)
      } catch (error) {
        handlers.onError?.(error as Error)
      }
    }

    currentEvent = 'message'
    dataLines = []
  }

  try {
    while (!signal.aborted) {
      const { value, done } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      let newlineIndex = buffer.indexOf('\n')
      while (newlineIndex >= 0) {
        const rawLine = buffer.slice(0, newlineIndex)
        buffer = buffer.slice(newlineIndex + 1)
        const line = rawLine.replace(/\r$/, '')

        if (!line) {
          flush()
        } else if (line.startsWith('event:')) {
          currentEvent = line.slice(6).trim()
        } else if (line.startsWith('data:')) {
          dataLines.push(line.slice(5).trimStart())
        }

        newlineIndex = buffer.indexOf('\n')
      }
    }
    flush()
  } finally {
    reader.releaseLock()
  }
}

export async function subscribeNotifications(handlers: StreamHandlers, signal: AbortSignal) {
  const authHeaders = buildAuthHeaders()
  if (!authHeaders) {
    throw new Error('AUTH_REQUIRED')
  }

  try {
    await openNotificationStream(authHeaders, signal, handlers)
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error)
    if (message === 'UNAUTHORIZED') {
      const accessToken = await reissueAccessToken()
      if (signal.aborted) return
      const retryHeaders = buildAuthHeaders()
      if (!retryHeaders) throw new Error('AUTH_REQUIRED')
      retryHeaders.Authorization = `Bearer ${accessToken}`
      await openNotificationStream(retryHeaders, signal, handlers)
      return
    }
    throw error
  }
}

export async function isNotificationStreamSupported() {
  return typeof fetch === 'function' && typeof ReadableStream !== 'undefined'
}
