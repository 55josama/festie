import { useMemo, useState, type ReactNode } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { deleteEvent, getEvent } from '../api/events'
import { getChatMessages, getChatRoomByEventId, sendChatMessage } from '../api/chat'
import { createCalendar } from '../api/calendar'
import ReportButton from '../components/ReportButton'
import { useAuthStore } from '../store/authStore'
import { formatPrice, formatDateTime } from '../lib/format'
import { getDDay as calcDDay } from '../lib/format'
import type { ChatRoom } from '../types'

export default function EventDetail() {
  const { eventId = '' } = useParams()
  const queryClient = useQueryClient()
  const { user, isLoggedIn } = useAuthStore()
  const [message, setMessage] = useState('')

  const { data: event, isLoading } = useQuery({
    queryKey: ['event', eventId],
    queryFn: () => getEvent(eventId),
    enabled: !!eventId,
  })

  const { data: chatRoom } = useQuery({
    queryKey: ['chat-room', eventId],
    queryFn: () => getChatRoomByEventId(eventId),
    enabled: !!eventId,
  })
  const chatRoomId = chatRoom?.chatRoomId

  const { data: messages = [] } = useQuery({
    queryKey: ['chat-messages', chatRoomId],
    queryFn: () => {
      if (!chatRoomId) {
        return Promise.resolve([])
      }
      return getChatMessages(chatRoomId)
    },
    enabled: !!chatRoomId,
    refetchInterval: 5000,
  })

  const sendMutation = useMutation({
    mutationFn: (content: string) => {
      if (!chatRoomId) {
        throw new Error('chatRoomId is missing')
      }
      return sendChatMessage(chatRoomId, content)
    },
    onSuccess: async () => {
      setMessage('')
      await queryClient.invalidateQueries({ queryKey: ['chat-messages', chatRoomId] })
    },
  })

  const calendarMutation = useMutation({
    mutationFn: ({ eventDate, memo }: { eventDate: string; memo?: string }) =>
      createCalendar({ eventId: event!.id, eventDate, memo }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['calendars'] })
      window.alert('내 일정에 추가했어요.')
    },
  })

  const deleteMutation = useMutation({
    mutationFn: () => {
      if (!event) throw new Error('event is missing')
      return deleteEvent(event.id)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['events'] })
      queryClient.invalidateQueries({ queryKey: ['event', eventId] })
      window.alert('행사를 삭제했어요.')
    },
  })

  const schedules = useMemo(() => event?.schedules ?? [], [event?.schedules])
  const chatState = useMemo(() => resolveChatState(chatRoom), [chatRoom])

  if (isLoading) {
    return <div className="px-5 py-10 text-slate-500">행사 정보를 불러오는 중입니다.</div>
  }

  if (!event) {
    return <div className="px-5 py-10 text-slate-500">행사를 찾을 수 없어요.</div>
  }

  return (
    <div className="px-5 py-6 md:px-8 md:py-8">
      <div className="mb-4 flex items-center gap-2 text-sm text-slate-500">
        <Link to="/" className="hover:text-slate-900">홈</Link>
        <span>/</span>
        <Link to="/events" className="hover:text-slate-900">행사</Link>
        <span>/</span>
        <span className="text-slate-900">{event.name}</span>
      </div>

      <div className="grid gap-6 xl:grid-cols-[1.25fr_0.75fr]">
        <section className="space-y-6 rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-6">
          <div className="flex flex-wrap items-center gap-2">
            <Badge>{event.categoryName}</Badge>
            <Badge tone="slate">{event.status}</Badge>
            {event.ticketingOpenAt && <Badge tone="blue">{calcDDay(event.ticketingOpenAt)}</Badge>}
          </div>

          <div className="grid gap-5 lg:grid-cols-[1fr_0.95fr]">
            <div className="overflow-hidden rounded-[26px] border border-[var(--line)] bg-white">
              {event.img ? (
                <img src={event.img} alt={event.name} className="block h-[320px] w-full object-cover" />
              ) : (
                <div className="flex h-[320px] items-center justify-center bg-gradient-to-br from-sky-50 to-white text-slate-400">
                  행사 이미지
                </div>
              )}
            </div>

            <div className="space-y-5">
              <div>
                <h1 className="text-[26px] font-black tracking-tight text-slate-950 md:text-[30px]">{event.name}</h1>
                <p className="mt-2 text-[15px] leading-6 text-slate-600">{event.description ?? '행사 상세 소개가 준비되어 있습니다.'}</p>
              </div>

              <div className="space-y-3">
                <div className="grid gap-3 sm:grid-cols-2">
                  <MiniInfoCard label="시작" value={formatDateTime(event.startAt)} />
                  <MiniInfoCard label="종료" value={formatDateTime(event.endAt)} />
                </div>
                <div className="overflow-hidden rounded-[18px] border border-[var(--line)] bg-white">
                  <DetailMeta label="장소" value={event.place} />
                  <DetailMeta label="가격" value={formatPrice(event.minFee, event.maxFee)} />
                  <DetailMeta label="출연" value={event.performer ?? '정보 없음'} />
                  <DetailMeta label="티켓팅" value={event.ticketingOpenAt ? formatDateTime(event.ticketingOpenAt) : '미정'} />
                </div>
              </div>

              <div className="flex flex-wrap gap-3">
                {event.hasTicketing ? event.ticketingLink ? (
                  <a href={event.ticketingLink} target="_blank" rel="noreferrer" className={ticketingButtonClass}>
                    티켓팅 바로가기
                  </a>
                ) : (
                  <span className={ticketingButtonClass} aria-disabled="true">
                    티켓팅 준비중
                  </span>
                ) : null}
                {event.officialLink && (
                  <a href={event.officialLink} target="_blank" rel="noreferrer" className="rounded-full border border-[var(--line)] bg-white px-5 py-3 text-sm font-semibold text-slate-700">
                    공식 링크
                  </a>
                )}
                {isLoggedIn() ? (
                  <button
                    onClick={() => calendarMutation.mutate({ eventDate: event.startAt, memo: event.name })}
                    className="rounded-full border border-[var(--line)] bg-white px-5 py-3 text-sm font-medium text-slate-700"
                  >
                    {calendarMutation.isPending ? '추가 중...' : '추가'}
                  </button>
                ) : (
                  <Link to="/login" className="rounded-full border border-[var(--line)] bg-white px-5 py-3 text-sm font-medium text-slate-700">
                    추가
                  </Link>
                )}
                {isLoggedIn() && <ReportButton targetType="EVENT" targetId={event.id} />}
                {user && /ADMIN|MANAGER/.test(user.role) && (
                  <button
                    type="button"
                    onClick={() => {
                      if (window.confirm('이 행사를 삭제할까요?')) {
                        deleteMutation.mutate()
                      }
                    }}
                    className="rounded-full border border-rose-200 bg-rose-50 px-5 py-3 text-sm font-semibold text-rose-700"
                  >
                    {deleteMutation.isPending ? '삭제 중...' : '행사 삭제'}
                  </button>
                )}
              </div>
            </div>
          </div>

          <div className="space-y-3">
            <h2 className="text-[18px] font-black tracking-tight text-slate-950">일정 세부</h2>
            <div className="grid gap-3 md:grid-cols-2">
              {schedules.length === 0 ? (
                <div className="rounded-[22px] border border-[var(--line)] bg-slate-50 p-5 text-sm text-slate-500 md:col-span-2">
                  등록된 세부 일정이 없습니다.
                </div>
              ) : schedules.map((schedule: any, index: number) => (
                <div key={schedule.id ?? index} className="rounded-[22px] border border-[var(--line)] bg-white p-4">
                  <div className="flex items-start justify-between gap-3">
                    <div className="min-w-0">
                      <div className="text-sm font-semibold text-[var(--accent)]">스케줄 {index + 1}</div>
                      <div className="mt-2 text-[16px] font-semibold text-slate-950">{schedule.title ?? schedule.memo ?? '일정'}</div>
                      <div className="mt-1 text-sm text-slate-500">{formatDateTime(schedule.startAt)} - {formatDateTime(schedule.endAt)}</div>
                    </div>
                    {isLoggedIn() && (
                      <button
                        type="button"
                        onClick={() => calendarMutation.mutate({ eventDate: schedule.startAt, memo: schedule.title ?? schedule.memo ?? event.name })}
                        className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-[11px] font-semibold text-slate-700"
                      >
                        추가
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </section>

        <aside className="space-y-4 rounded-[24px] border border-[var(--line)] bg-white p-4 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-5 xl:sticky xl:top-24 xl:self-start">
          <div className="flex items-center justify-between">
            <div>
              <div className="text-sm font-semibold text-slate-500">실시간 채팅</div>
              <div className="text-[18px] font-black tracking-tight text-slate-950">{chatRoom?.eventName ?? event.name}</div>
            </div>
            <div className={`rounded-full px-3 py-1 text-xs font-semibold ${chatState.badgeClass}`}>
              {chatState.label}
            </div>
          </div>

          <div className="grid gap-3 sm:grid-cols-2">
            <MiniInfoCard label="오픈 시간" value={chatState.openTimeLabel} />
            <MiniInfoCard label="클로즈 시간" value={chatState.closeTimeLabel} />
          </div>

          <div className={`space-y-2 rounded-[20px] border border-[var(--line)] p-3.5 transition-opacity ${chatState.isOpen ? 'bg-white' : 'bg-slate-50 opacity-55'}`}>
            <div className="flex items-center justify-between">
              <div className="text-sm font-semibold text-slate-500">메시지</div>
              <div className="text-xs text-slate-400">{messages.length}개</div>
            </div>
            <div className="max-h-[460px] space-y-2 overflow-y-auto pr-1">
              {messages.map((msg: any) => (
                <MessageBubble key={msg.messageId} message={msg} me={user?.userId} />
              ))}
            </div>
          </div>

          <div className="rounded-[20px] border border-[var(--line)] bg-slate-50 p-3.5">
            {!chatState.isOpen ? (
              <div className="space-y-1 text-sm text-slate-500">
                <div className="font-semibold text-slate-700">{chatState.closedReason}</div>
                <div>{chatState.helperText}</div>
              </div>
            ) : isLoggedIn() ? (
              <div className="flex gap-2">
                <input
                  value={message}
                  onChange={(e) => setMessage(e.target.value)}
                  placeholder="메시지를 입력하세요"
                  className="min-w-0 flex-1 rounded-full border border-[var(--line)] bg-white px-3 py-2.5 text-sm outline-none"
                />
                <button
                  onClick={() => message.trim() && sendMutation.mutate(message)}
                  className="rounded-full bg-[var(--accent)] px-3.5 py-2.5 text-sm font-semibold text-white"
                >
                  전송
                </button>
              </div>
            ) : (
              <div className="text-sm text-slate-500">
                <Link to="/login" className="font-semibold text-[var(--accent)]">로그인</Link> 후 채팅에 참여할 수 있어요.
              </div>
            )}
          </div>
        </aside>
      </div>
    </div>
  )
}

function resolveChatState(chatRoom?: ChatRoom) {
  const now = new Date()
  if (!chatRoom) {
    return {
      isOpen: false,
      label: '채팅방 정보 없음',
      badgeClass: 'bg-slate-100 text-slate-600',
      openTimeLabel: '정보 없음',
      closeTimeLabel: '정보 없음',
      closedReason: '채팅방 정보를 불러오는 중입니다.',
      helperText: '잠시 후 다시 시도해주세요.',
    }
  }
  const status = String(chatRoom?.status ?? '').toUpperCase()
  const openAtRaw = chatRoom?.openedAt ?? chatRoom?.scheduledOpenAt ?? null
  const closeAtRaw = chatRoom?.closedAt ?? chatRoom?.scheduledCloseAt ?? null
  const openAt = openAtRaw ? new Date(openAtRaw) : null
  const closeAt = closeAtRaw ? new Date(closeAtRaw) : null

  if (status === 'CLOSED') {
    return {
      isOpen: false,
      label: '채팅방 종료',
      badgeClass: 'bg-slate-100 text-slate-600',
      openTimeLabel: openAtRaw ? formatDateTime(openAtRaw) : '정보 없음',
      closeTimeLabel: closeAtRaw ? formatDateTime(closeAtRaw) : '미정',
      closedReason: '채팅방이 닫혔습니다.',
      helperText: '행사 채팅은 끝났지만, 상세 정보는 계속 볼 수 있어요.',
    }
  }

  if (status === 'SCHEDULED') {
    if (closeAt && now > closeAt) {
      return {
        isOpen: false,
        label: '채팅방 종료',
        badgeClass: 'bg-slate-100 text-slate-600',
        openTimeLabel: openAtRaw ? formatDateTime(openAtRaw) : '정보 없음',
        closeTimeLabel: formatDateTime(closeAtRaw),
        closedReason: '채팅방이 닫혔습니다.',
        helperText: '행사 채팅은 끝났지만, 상세 정보는 계속 볼 수 있어요.',
      }
    }

    const waitingForOpen = openAt && now < openAt
    return {
      isOpen: false,
      label: waitingForOpen ? '채팅방 오픈 전' : '오픈 예정',
      badgeClass: 'bg-slate-100 text-slate-600',
      openTimeLabel: openAtRaw ? formatDateTime(openAtRaw) : '정보 없음',
      closeTimeLabel: closeAtRaw ? formatDateTime(closeAtRaw) : '미정',
      closedReason: waitingForOpen ? '채팅방이 오픈 전 입니다.' : '채팅방이 아직 열리지 않았습니다.',
      helperText: waitingForOpen ? '오픈 시간 이후에 메시지를 보낼 수 있어요.' : '예정 시간이 지나면 관리자 강제 오픈으로 열 수 있어요.',
    }
  }

  if (closeAt && now > closeAt) {
    return {
      isOpen: false,
      label: '채팅방 종료',
      badgeClass: 'bg-slate-100 text-slate-600',
      openTimeLabel: openAtRaw ? formatDateTime(openAtRaw) : '정보 없음',
      closeTimeLabel: formatDateTime(closeAtRaw),
      closedReason: '채팅방이 닫혔습니다.',
      helperText: '행사 채팅은 끝났지만, 상세 정보는 계속 볼 수 있어요.',
    }
  }

  if (openAt && now < openAt) {
    return {
      isOpen: false,
      label: '채팅방 오픈 전',
      badgeClass: 'bg-slate-100 text-slate-600',
      openTimeLabel: formatDateTime(openAtRaw),
      closeTimeLabel: closeAtRaw ? formatDateTime(closeAtRaw) : '미정',
      closedReason: '채팅방이 오픈 전 입니다.',
      helperText: '오픈 시간 이후에 메시지를 보낼 수 있어요.',
    }
  }

  return {
    isOpen: true,
    label: '오픈중',
    badgeClass: 'bg-[var(--accent-soft)] text-[var(--accent)]',
    openTimeLabel: openAtRaw ? formatDateTime(openAtRaw) : '정보 없음',
    closeTimeLabel: closeAtRaw ? formatDateTime(closeAtRaw) : '미정',
    closedReason: '',
    helperText: '',
  }
}

function Badge({ children, tone = 'violet' }: { children: ReactNode; tone?: 'violet' | 'slate' | 'blue' }) {
  const style = tone === 'slate'
    ? 'bg-slate-100 text-slate-600'
    : tone === 'blue'
      ? 'bg-[var(--accent-soft)] text-[var(--accent)]'
      : 'bg-violet-50 text-violet-600'
  return <span className={`rounded-full px-3 py-1 text-xs font-semibold ${style}`}>{children}</span>
}

const ticketingButtonClass = 'inline-flex items-center justify-center rounded-full border border-[var(--line)] bg-[var(--accent-soft)] px-5 py-3 text-sm font-semibold text-[var(--accent)]'

function MiniInfoCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-[18px] border border-[var(--line)] bg-white px-4 py-3">
      <div className="text-[11px] font-semibold uppercase tracking-[0.12em] text-slate-500">{label}</div>
      <div className="mt-1 text-sm font-semibold text-slate-900">{value}</div>
    </div>
  )
}

function DetailMeta({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between gap-4 border-b border-[var(--line)] px-4 py-3 last:border-b-0">
      <div className="text-[11px] font-semibold uppercase tracking-[0.12em] text-slate-500">{label}</div>
      <div className="min-w-0 flex-1 text-right text-sm font-semibold text-slate-900">{value}</div>
    </div>
  )
}

function MessageBubble({ message, me }: { message: any; me?: string }) {
  const mine = me && message.userId === me
  if (message.messageType === 'SYSTEM') {
    return (
      <div className="flex justify-center">
        <span className="rounded-full bg-slate-100 px-2.5 py-0.5 text-[11px] text-slate-500">{message.content}</span>
      </div>
    )
  }

  return (
    <div className={`flex flex-col ${mine ? 'items-end' : 'items-start'}`}>
      <div className={`max-w-[76%] rounded-[16px] px-3 py-2 text-[13px] leading-5 ${mine ? 'bg-[var(--accent)] text-white' : 'bg-slate-100 text-slate-800'}`}>
        <div className="mb-1 text-[10px] font-semibold opacity-70">{message.writerNickname}</div>
        {message.content}
      </div>
      <div className="mt-1 text-[10px] text-slate-400">{formatDateTime(message.createdAt)}</div>
    </div>
  )
}
