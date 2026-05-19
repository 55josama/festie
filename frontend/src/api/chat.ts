import client from './client'
import { unwrap, unwrapPage, unwrapPageResponse } from '../lib/api'
import type { ChatMessage, ChatRoom } from '../types'
import type { AdminMessageItem } from '../types/admin'

export interface ChatMessageSliceResponse {
  messages: ChatMessage[]
  hasNext: boolean
}

export const createChatRoom = async (payload: {
  eventId: string
  eventName: string
  category: string
  scheduledOpenAt: string
  scheduledCloseAt: string
}) => {
  const res = await client.post('/chat-service/v1/chat/admin/rooms', payload)
  return unwrap<ChatRoom>(res.data)
}

export const getChatRoomByEventId = async (eventId: string) => {
  const res = await client.get('/chat-service/v1/chat/rooms/event', { params: { eventId } })
  return unwrap<ChatRoom>(res.data)
}

export const verifyEventLocation = async (eventId: string, currentLatitude: number, currentLongitude: number) => {
  const res = await client.post(`/chat-service/v1/chat/events/${eventId}/location/verify`, {
    currentLatitude,
    currentLongitude,
  })
  return unwrap<{ eventId: string; isNearEvent: boolean }>(res.data)
}

export const getChatMessages = async (chatRoomId: string, params: { page?: number; size?: number } = {}) => {
  const res = await client.get(`/chat-service/v1/chat/rooms/${chatRoomId}/messages`, {
    params: {
      page: params.page ?? 0,
      size: params.size ?? 30,
    },
  })
  const data = unwrap<any>(res.data)
  const normalized = Array.isArray(data) ? data : (data?.messages ?? data?.content ?? [])
  return {
    messages: unwrapPage<ChatMessage>({ data: normalized }),
    hasNext: Boolean(data?.hasNext),
  } satisfies ChatMessageSliceResponse
}

export const sendChatMessage = async (chatRoomId: string, content: string) => {
  const res = await client.post(`/chat-service/v1/chat/rooms/${chatRoomId}/messages`, { content })
  return unwrap<ChatMessage>(res.data)
}

export const deleteChatMessage = async (messageId: string) => {
  const res = await client.delete(`/chat-service/v1/chat/messages/${messageId}`)
  return unwrap(res.data)
}

export const getPopularChatRooms = async (limit = 3) => {
  const res = await client.get('/chat-service/v1/chat/rooms/popular', { params: { limit } })
  return unwrapPage<ChatRoom>(res.data)
}

export const getAdminChatRooms = async (params: Record<string, any> = {}) => {
  const res = await client.get('/chat-service/v1/chat/admin/rooms', { params })
  return unwrapPageResponse<ChatRoom & { currentViewerCount?: number }>(res.data)
}

export const getAdminChatMessages = async (params: Record<string, any> = {}) => {
  const res = await client.get('/chat-service/v1/chat/admin/messages', { params })
  return unwrapPageResponse<AdminMessageItem>(res.data)
}

export const getAdminChatMessage = async (messageId: string) => {
  const res = await client.get(`/chat-service/v1/chat/admin/messages/${messageId}`)
  return unwrap<AdminMessageItem>(res.data)
}

export const updateAdminChatMessageStatus = async (messageId: string, status: 'ACTIVE' | 'BLINDED') => {
  const res = await client.patch(`/chat-service/v1/chat/admin/messages/${messageId}/status`, { status })
  return unwrap<AdminMessageItem>(res.data)
}
