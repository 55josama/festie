import { useMemo, type ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getEvents, getTicketingEvents } from '../api/events'
import { getPosts } from '../api/community'
import { getPopularChatRooms } from '../api/chat'
import { formatDateRange } from '../lib/format'
import type { Event, Post } from '../types'

export default function Home() {
  const { data: allEvents = [] } = useQuery({ queryKey: ['events', 'home'], queryFn: () => getEvents({ size: 100 }) })
  const { data: ticketingEvents = [] } = useQuery({ queryKey: ['events', 'ticketing'], queryFn: getTicketingEvents })
  const { data: posts = [] } = useQuery({ queryKey: ['posts', 'home'], queryFn: () => getPosts({ size: 4, sort: 'createdAt,desc' }) })
  const { data: popularRooms = [] } = useQuery({ queryKey: ['popular-chat-rooms', 'home'], queryFn: () => getPopularChatRooms(3) })

  const featuredPosts = useMemo(() => pickTodayPopularPosts(posts as Post[]), [posts])
  const upcomingEvents = useMemo(() => getCurrentEventWindow(allEvents as Event[]), [allEvents])
  const upcomingTicketing = useMemo(() => getCurrentTicketingWindow(ticketingEvents as Event[]), [ticketingEvents])

  return (
    <div className="space-y-6 px-5 py-5 md:px-8 md:py-7">
      <section className="rounded-[24px] border border-[var(--line)] bg-white p-4 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-6">
        <div className="grid gap-5 xl:grid-cols-[1.2fr_0.8fr] xl:items-start">
          <div className="space-y-3">
            <div className="inline-flex rounded-full bg-[var(--accent-soft)] px-3 py-1 text-xs font-semibold text-[var(--accent)] md:inline-flex">
              행사 탐색
            </div>
            <h1 className="max-w-xl text-[22px] font-black tracking-tight text-slate-950 md:hidden">
              오늘 열릴 행사부터 바로 확인해보세요
            </h1>
            <p className="max-w-lg text-sm leading-6 text-slate-600 md:hidden">
              찾고, 보고, 바로 담는 행사 탐색
            </p>
            <h2 className="hidden max-w-xl text-[22px] font-black tracking-tight text-slate-950 md:block md:text-[26px]">
              오늘 열릴 행사부터 바로 확인해보세요
            </h2>
            <p className="hidden max-w-lg text-sm leading-6 text-slate-600 md:block">
              찾고, 보고, 바로 담는 행사 탐색
            </p>
          </div>
        </div>
      </section>

      <section className="grid gap-6 xl:grid-cols-[minmax(0,7fr)_minmax(0,3fr)]">
        <div className="min-w-0 space-y-4 rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
          <SectionHeading
            title="다가오는 행사"
            action={<Link to="/calendar" className="text-sm font-medium text-[var(--accent)]">캘린더</Link>}
          />
          {upcomingEvents.length ? (
            <div className="space-y-3">
              {upcomingEvents.slice(0, 6).map((event) => (
                <CompactEventRow key={event.id} event={event} />
              ))}
            </div>
          ) : (
            <EmptyEventState />
          )}
        </div>

        <div className="min-w-0 space-y-4 rounded-[24px] border border-[var(--line)] bg-[#f7f5ff] p-4 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
          <SectionHeading
            title="곧 열리는 티켓팅"
            action={<Link to="/calendar" className="text-sm font-medium text-[var(--accent)]">더 보기</Link>}
          />
          <div className="space-y-3">
            {upcomingTicketing.slice(0, 5).map((event) => (
              <TicketingRow key={event.id} event={event} />
            ))}
          </div>
        </div>
      </section>

      <section className="hidden rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:block">
        <SectionHeading
          title="실시간 인기 채팅방"
        />
        <div className="mt-4 grid gap-3 md:grid-cols-3">
          {popularRooms.map((room: any) => (
            <Link
              key={room.chatRoomId}
              to={`/events/${room.eventId}`}
              className="rounded-[18px] border border-[var(--line)] bg-[#faf8ff] px-4 py-3 hover:bg-white"
            >
              <div className="text-xs font-semibold text-[var(--accent)]">{room.category}</div>
              <div className="mt-1 truncate text-sm font-semibold text-slate-950">{room.eventName}</div>
              <div className="mt-1 flex items-center justify-between text-[11px] text-slate-500">
                <span>{room.status}</span>
                <span>{room.currentViewerCount ?? 0}명</span>
              </div>
            </Link>
          ))}
        </div>
      </section>

      <section className="rounded-[24px] border border-[var(--line)] bg-white p-4 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-5">
        <SectionHeading
          title="오늘 인기글"
          action={<Link to="/community" className="hidden text-sm font-medium text-[var(--accent)] md:inline">전체보기</Link>}
        />
        <div className="mt-4 grid gap-3">
          {featuredPosts.map((post) => (
            <PostRow key={post.id} post={post} />
          ))}
        </div>
      </section>
    </div>
  )
}

function sortUpcomingEvents(events: Event[]) {
  return [...events].sort((a, b) => String(a.startAt ?? '').localeCompare(String(b.startAt ?? '')))
}

function sortUpcomingTicketing(events: Event[]) {
  return [...events]
    .filter((event) => event.hasTicketing)
    .sort((a, b) => String(a.ticketingOpenAt ?? '').localeCompare(String(b.ticketingOpenAt ?? '')))
}

function pickTodayPopularPosts(posts: Post[]) {
  const today = new Date().toLocaleDateString('ko-KR')
  const todayPosts = posts
    .filter((post) => new Date(post.createdAt).toLocaleDateString('ko-KR') === today)
    .sort((a, b) => (b.likeCount + b.commentCount) - (a.likeCount + a.commentCount))

  const ranked = todayPosts.length ? todayPosts : [...posts].sort((a, b) => (b.likeCount + b.commentCount) - (a.likeCount + a.commentCount))
  return ranked.slice(0, 4)
}

function countdownLabel(dateValue?: string | null, mode: 'event' | 'ticketing' = 'event') {
  if (!dateValue) return mode === 'ticketing' ? '미정' : '진행중'
  const diffDays = Math.ceil((new Date(dateValue).getTime() - startOfToday().getTime()) / 86400000)
  if (diffDays > 0) return `D-${diffDays}`
  return mode === 'ticketing' ? '오픈중' : '진행중'
}

function startOfToday() {
  const today = new Date()
  return new Date(today.getFullYear(), today.getMonth(), today.getDate())
}

function getCurrentEventWindow(events: Event[]) {
  return sortUpcomingEvents(events.filter((event) => isOngoingEvent(event) || isWithinDays(event.startAt, 7)))
}

function getCurrentTicketingWindow(events: Event[]) {
  return sortUpcomingTicketing(
    events.filter((event) => {
      if (!event.hasTicketing || !event.ticketingOpenAt) return false
      return isWithinDays(event.ticketingOpenAt, 7) || (isTicketingOpen(event) && isWithinDays(event.startAt, 7))
    })
  )
}

function isOngoingEvent(event: Event) {
  const now = new Date()
  const start = new Date(event.startAt)
  const end = new Date(event.endAt)
  return start <= now && now <= end
}

function isTicketingOpen(event: Event) {
  if (!event.ticketingOpenAt) return false
  return new Date(event.ticketingOpenAt) <= new Date()
}

function isWithinDays(dateValue?: string | null, limit = 7) {
  if (!dateValue) return false
  const target = new Date(dateValue)
  const diffDays = Math.ceil((target.getTime() - startOfToday().getTime()) / 86400000)
  return diffDays >= 0 && diffDays <= limit
}

function SectionHeading({ title, action }: { title: string; action?: ReactNode }) {
  return (
    <div className="flex items-center justify-between">
      <h2 className="text-[18px] font-black tracking-tight text-slate-950">{title}</h2>
      {action}
    </div>
  )
}

function CompactEventRow({ event }: { event: Event }) {
  const chipClass = categoryChipClass(event.categoryName)
  return (
    <Link
      to={`/events/${event.id}`}
      className="flex items-center justify-between gap-4 rounded-[18px] border border-[var(--line)] bg-slate-50 px-4 py-3 hover:bg-white"
    >
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <span className={`rounded-full px-2.5 py-1 text-[11px] font-semibold ${chipClass}`}>{event.categoryName}</span>
          <span className="text-[11px] text-slate-500">{event.status}</span>
        </div>
        <div className="mt-2 truncate text-sm font-semibold text-slate-950">{event.name}</div>
        <div className="mt-1 truncate text-xs text-slate-500">
          {formatDateRange(event.startAt, event.endAt)} · {event.place}
        </div>
      </div>
      <div className="shrink-0 text-right">
        <div className="text-xs font-semibold text-[var(--accent)]">{countdownLabel(event.startAt)}</div>
      </div>
    </Link>
  )
}

function EmptyEventState() {
  return (
    <div className="rounded-[20px] border border-dashed border-[var(--line)] bg-slate-50 p-5">
      <div className="text-sm font-semibold text-slate-900">일정이 없네요.. 😮‍💨</div>
      <p className="mt-1 text-sm text-slate-500">
        원하시는 일정이 있나요? <Link to="/community/new" className="font-semibold text-[var(--accent)] underline decoration-[var(--accent-soft)] decoration-2 underline-offset-4 transition-colors hover:text-[var(--accent-dark)]">요청하러 가기!</Link>
      </p>
    </div>
  )
}

function TicketingRow({ event }: { event: Event }) {
  const chipClass = categoryChipClass(event.categoryName)
  return (
    <Link
      to={`/events/${event.id}`}
      className="block rounded-[18px] border border-[#dcd6f6] bg-gradient-to-br from-[#faf8ff] to-white px-4 py-3 hover:border-[var(--accent)]"
    >
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0 flex-1">
          <div className={`inline-flex rounded-full px-2.5 py-1 text-[11px] font-semibold ${chipClass}`}>{event.categoryName}</div>
          <div className="mt-1 truncate text-sm font-semibold text-slate-950">{event.name}</div>
          <div className="mt-1 truncate text-xs text-slate-500">{formatDateRange(event.startAt, event.endAt)}</div>
          <div className="mt-2 text-xs text-slate-500">{event.place}</div>
        </div>
        <div className="shrink-0 rounded-full bg-[var(--accent-soft)] px-2.5 py-1 text-[11px] font-semibold text-[var(--accent)]">
          {countdownLabel(event.ticketingOpenAt, 'ticketing')}
        </div>
      </div>
    </Link>
  )
}

function PostRow({ post }: { post: Post }) {
  const chipClass = postChipClass(post.categoryName)
  return (
    <Link
      to={`/community/${post.id}`}
      className="flex items-center justify-between gap-4 rounded-[18px] border border-[var(--line)] bg-slate-50 px-4 py-3 hover:bg-white"
    >
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <span className={`rounded-full px-2.5 py-1 text-[11px] font-semibold ${chipClass}`}>{post.categoryName}</span>
          {post.eventName && <span className="text-[11px] text-slate-500">{post.eventName}</span>}
        </div>
        <div className="mt-2 truncate text-sm font-semibold text-slate-950">{post.title}</div>
        <div className="mt-1 truncate text-xs text-slate-500">{post.authorNickname ?? '익명'} · {post.createdAt}</div>
      </div>
      <div className="shrink-0 text-right text-xs text-slate-500">
        <div>♡ {post.likeCount}</div>
        <div>💬 {post.commentCount}</div>
      </div>
    </Link>
  )
}

function categoryChipClass(name: string) {
  return (
    {
      콘서트: 'bg-violet-100 text-violet-700',
      축제: 'bg-fuchsia-100 text-fuchsia-700',
      팬미팅: 'bg-pink-100 text-pink-700',
      팝업스토어: 'bg-sky-100 text-sky-700',
    }[name] ?? 'bg-[var(--accent-soft)] text-[var(--accent)]'
  )
}

function postChipClass(name: string) {
  return (
    {
      후기: 'bg-violet-100 text-violet-700',
      꿀팁: 'bg-sky-100 text-sky-700',
      자유: 'bg-emerald-100 text-emerald-700',
      요청: 'bg-rose-100 text-rose-700',
    }[name] ?? 'bg-[var(--accent-soft)] text-[var(--accent)]'
  )
}
