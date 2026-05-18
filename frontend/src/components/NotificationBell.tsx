import { useEffect, useMemo, useRef, useState } from 'react'
import { useAuthStore } from '../store/authStore'
import type { Notification } from '../types'
import {
  deleteNotification,
  getNotifications,
  markAllNotificationsAsRead,
  subscribeNotifications,
} from '../api/notifications'

type NotificationBellProps = {
  variant: 'mobile' | 'desktop'
}

const PAGE_SIZE = 5

function useMediaQuery(query: string) {
  const [matches, setMatches] = useState(() => window.matchMedia(query).matches)

  useEffect(() => {
    const mediaQueryList = window.matchMedia(query)
    const handleChange = () => setMatches(mediaQueryList.matches)
    handleChange()
    mediaQueryList.addEventListener('change', handleChange)
    return () => mediaQueryList.removeEventListener('change', handleChange)
  }, [query])

  return matches
}

function BellIcon() {
  return (
    <svg viewBox="0 0 24 24" className="h-5 w-5" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <path d="M6.5 8.5a5.5 5.5 0 1 1 11 0c0 5 2 5.5 2 8H4.5c0-2.5 2-3 2-8Z" />
      <path d="M9.5 18.5a2.5 2.5 0 0 0 5 0" />
    </svg>
  )
}

export default function NotificationBell({ variant }: NotificationBellProps) {
  const isDesktop = useMediaQuery('(min-width: 960px)')
  const shouldRender = variant === 'desktop' ? isDesktop : !isDesktop
  const user = useAuthStore((state) => state.user)
  const accessToken = useAuthStore((state) => state.accessToken)
  const syncUserFromAccessToken = useAuthStore((state) => state.syncUserFromAccessToken)
  const isLoggedIn = useAuthStore((state) => state.isLoggedIn)

  const [open, setOpen] = useState(false)
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading] = useState(false)
  const [loadingMore, setLoadingMore] = useState(false)
  const [liveConnected, setLiveConnected] = useState(false)

  const panelRef = useRef<HTMLDivElement | null>(null)
  const buttonRef = useRef<HTMLButtonElement | null>(null)

  const unreadCount = useMemo(() => notifications.filter((item) => item.readAt === null).length, [notifications])
  const hasPrev = page > 0
  const hasNext = page + 1 < totalPages
  const isMockDev = import.meta.env.DEV && String(import.meta.env.VITE_USE_MSW ?? 'true') === 'true'
  const canUseLiveStream = shouldRender && !isMockDev

  const syncInitialNotifications = async () => {
    if (!isLoggedIn() || !user?.userId) {
      setNotifications([])
      setPage(0)
      setTotalPages(1)
      setLiveConnected(false)
      return
    }

    try {
      setLoading(true)
      const pageResponse = await getNotifications({ page: 0, size: PAGE_SIZE })
      setNotifications(pageResponse.content)
      setPage(0)
      setTotalPages(pageResponse.totalPages)
    } finally {
      setLoading(false)
    }
  }

  const handleMarkAllRead = async () => {
    const updated = await markAllNotificationsAsRead()
    const updatedMap = new Map(updated.map((item) => [item.id, item]))
    setNotifications((prev) =>
      prev.map((item) => {
        const replacement = updatedMap.get(item.id)
        return replacement ? { ...item, readAt: replacement.readAt } : item
      }),
    )
  }

  const handleDelete = async (notificationId: string) => {
    await deleteNotification(notificationId)
    setNotifications((prev) => prev.filter((item) => item.id !== notificationId))
  }

  const handleLoadPage = async (nextPage: number) => {
    if (loadingMore || nextPage < 0 || nextPage >= totalPages || nextPage === page) return
    setLoadingMore(true)
    try {
      const pageResponse = await getNotifications({ page: nextPage, size: PAGE_SIZE })
      setNotifications(pageResponse.content)
      setPage(nextPage)
      setTotalPages(pageResponse.totalPages)
    } finally {
      setLoadingMore(false)
    }
  }

  useEffect(() => {
    syncUserFromAccessToken()
  }, [syncUserFromAccessToken])

  useEffect(() => {
    if (!shouldRender) return
    let cancelled = false
    let retryTimer: number | undefined
    const abortController = new AbortController()
    const pollingTimer = !canUseLiveStream && isLoggedIn() && user?.userId
      ? window.setInterval(() => {
          void syncInitialNotifications()
        }, 20_000)
      : undefined

    const start = async () => {
      if (!isLoggedIn() || !user?.userId) {
        return
      }

      await syncInitialNotifications()

      if (!canUseLiveStream) {
        setLiveConnected(false)
        return
      }

      const connect = async () => {
        if (cancelled || abortController.signal.aborted) return
        try {
          await subscribeNotifications(
            {
              onConnect: () => {
                if (!cancelled) setLiveConnected(true)
              },
              onNotification: (notification) => {
                if (cancelled) return
                setNotifications((prev) => {
                  const next = [notification, ...prev.filter((item) => item.id !== notification.id)]
                  return next
                })
              },
              onError: () => {
                if (!cancelled) setLiveConnected(false)
              },
            },
            abortController.signal,
          )
          if (cancelled || abortController.signal.aborted) return
          setLiveConnected(false)
          await syncInitialNotifications()
          retryTimer = window.setTimeout(connect, 5_000)
        } catch {
          if (cancelled || abortController.signal.aborted) return
          setLiveConnected(false)
          await syncInitialNotifications()
          retryTimer = window.setTimeout(connect, 5_000)
        }
      }

      void connect()
    }

    void start()

    return () => {
      cancelled = true
      abortController.abort()
      if (retryTimer) {
        window.clearTimeout(retryTimer)
      }
      if (pollingTimer) {
        window.clearInterval(pollingTimer)
      }
    }
  }, [shouldRender, canUseLiveStream, accessToken, user?.userId, user?.role])

  useEffect(() => {
    const handlePointerDown = (event: MouseEvent) => {
      if (!open) return
      const target = event.target as Node
      if (panelRef.current?.contains(target) || buttonRef.current?.contains(target)) return
      setOpen(false)
    }

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setOpen(false)
      }
    }

    document.addEventListener('mousedown', handlePointerDown)
    document.addEventListener('keydown', handleKeyDown)
    return () => {
      document.removeEventListener('mousedown', handlePointerDown)
      document.removeEventListener('keydown', handleKeyDown)
    }
  }, [open])

  if (!shouldRender || !isLoggedIn()) {
    return null
  }

  return (
    <div className="relative">
      <button
        ref={buttonRef}
        type="button"
        onClick={() => setOpen((value) => !value)}
        className={`relative inline-flex items-center justify-center rounded-full border border-white/60 bg-[linear-gradient(135deg,rgba(130,109,255,0.95),rgba(160,123,255,0.82),rgba(97,204,255,0.7))] text-white shadow-[0_14px_30px_rgba(129,104,255,0.26)] transition-transform hover:-translate-y-0.5 ${
          variant === 'desktop' ? 'h-11 w-11' : 'h-10 w-10'
        }`}
        aria-label="알림 열기"
      >
        <BellIcon />
        {unreadCount > 0 && <span className="absolute -right-1 -top-1 h-3 w-3 rounded-full border-2 border-white bg-rose-500 shadow-sm" />}
      </button>

      {open && (
        <div
          ref={panelRef}
          className={`fixed z-[70] overflow-hidden rounded-[24px] border border-[var(--line)] bg-white shadow-[0_24px_60px_rgba(15,23,42,0.18)] ${
            variant === 'desktop'
              ? 'right-4 top-16 w-[min(92vw,380px)]'
              : 'right-3 top-16 w-[min(84vw,320px)] max-h-[72vh]'
          }`}
        >
          <div className="flex items-start justify-between gap-3 border-b border-slate-100 bg-gradient-to-r from-slate-950 to-slate-800 px-4 py-3 text-white">
            <div>
              <div className="inline-flex rounded-full bg-white/10 px-2.5 py-1 text-[11px] font-semibold text-slate-100">
                알림
              </div>
              <div className="mt-1 text-[16px] font-black tracking-tight">Festie Notifications</div>
              <div className="mt-0.5 text-[11px] text-slate-300">
                {liveConnected ? '실시간 연결 중' : 'DB 저장 알림'}
              </div>
            </div>
            <button
              type="button"
              onClick={() => setOpen(false)}
              className="rounded-full bg-white/10 px-3 py-2 text-xs font-semibold text-white"
            >
              닫기
            </button>
          </div>

          <div className="max-h-[calc(72vh-64px)] overflow-y-auto bg-[linear-gradient(180deg,#f8fafc_0%,#ffffff_100%)] px-4 py-3">
            <div className="flex items-center justify-between gap-2">
              <div className="text-[12px] font-semibold text-slate-500">받은 알림 {notifications.length}건</div>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => void syncInitialNotifications()}
                  className="rounded-full border border-slate-200 bg-white px-3 py-1.5 text-[11px] font-semibold text-slate-600 transition-colors hover:bg-slate-50"
                >
                  새로고침
                </button>
                <button
                  type="button"
                  onClick={() => void handleMarkAllRead()}
                  className="rounded-full bg-[var(--accent)] px-3 py-1.5 text-[11px] font-semibold text-white shadow-sm transition-opacity hover:opacity-90 disabled:opacity-40"
                  disabled={notifications.every((item) => item.readAt !== null)}
                >
                  모두 읽음
                </button>
              </div>
            </div>

            <div className="mt-3 max-h-[420px] space-y-2 overflow-y-auto pr-1">
              {loading ? (
                <div className="rounded-[18px] border border-dashed border-slate-200 bg-white px-4 py-5 text-center text-sm text-slate-500">
                  알림을 불러오는 중이에요.
                </div>
              ) : notifications.length === 0 ? (
                <div className="rounded-[18px] border border-dashed border-slate-200 bg-white px-4 py-5 text-center text-sm text-slate-500">
                  아직 받은 알림이 없어요.
                </div>
              ) : (
                notifications.map((notification) => (
                  <article
                    key={notification.id}
                    className={`group rounded-[18px] border p-3 shadow-sm transition-colors ${
                      notification.readAt
                        ? 'border-slate-100 bg-white'
                        : 'border-[var(--accent-soft)]/60 bg-[var(--accent-soft)]/35'
                    }`}
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div className="min-w-0 flex-1">
                        <div className="flex items-center gap-2">
                          <span className="inline-flex rounded-full bg-slate-100 px-2.5 py-1 text-[10px] font-semibold text-slate-600">
                            {notification.readAt ? '읽음' : '새 알림'}
                          </span>
                        </div>
                        <div className="mt-2 text-[13px] font-semibold leading-5 text-slate-950">
                          {notification.title}
                        </div>
                        <div className="mt-1 text-[12px] leading-5 text-slate-600">{notification.content}</div>
                      </div>

                      <button
                        type="button"
                        onClick={() => void handleDelete(notification.id)}
                        className="rounded-full border border-slate-200 bg-white px-2.5 py-1.5 text-[11px] font-semibold text-slate-500 transition-colors hover:border-rose-200 hover:text-rose-600"
                        aria-label="알림 삭제"
                      >
                        삭제
                      </button>
                    </div>
                  </article>
                ))
              )}
            </div>

            <div className="mt-3 flex items-center justify-center gap-2">
              <button
                type="button"
                onClick={() => void handleLoadPage(page - 1)}
                disabled={!hasPrev || loadingMore}
                className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:opacity-40"
              >
                이전
              </button>
              <div className="flex items-center gap-1">
                {Array.from({ length: Math.max(totalPages, 1) }, (_, index) => index).map((pageIndex) => (
                  <button
                    key={pageIndex}
                    type="button"
                    onClick={() => void handleLoadPage(pageIndex)}
                    disabled={loadingMore}
                    className={`h-8 min-w-8 rounded-full px-2 text-xs font-semibold transition-colors ${
                      page === pageIndex ? 'bg-[var(--accent-soft)] text-[var(--accent)]' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                    }`}
                  >
                    {pageIndex + 1}
                  </button>
                ))}
              </div>
              <button
                type="button"
                onClick={() => void handleLoadPage(page + 1)}
                disabled={!hasNext || loadingMore}
                className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:opacity-40"
              >
                다음
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
