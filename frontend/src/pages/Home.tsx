import { useMemo, type ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getCategories } from '../api/community'
import { getEvents, getTicketingEvents } from '../api/events'
import { getPosts } from '../api/community'
import { getPopularChatRooms } from '../api/chat'
import { formatDateRange } from '../lib/format'
import type { Event, Post } from '../types'

export default function Home() {
  const { data: allEvents = [] } = useQuery({ queryKey: ['events', 'home'], queryFn: () => getEvents({ size: 100 }) })
  const { data: ticketingEvents = [] } = useQuery({ queryKey: ['events', 'ticketing'], queryFn: getTicketingEvents })
  const { data: posts = [] } = useQuery({ queryKey: ['posts', 'home'], queryFn: () => getPosts({ size: 4, sort: 'createdAt,desc' }) })
  const { data: categories = [] } = useQuery({ queryKey: ['categories', 'home'], queryFn: getCategories })
  const { data: popularRooms = [] } = useQuery({ queryKey: ['popular-chat-rooms', 'home'], queryFn: () => getPopularChatRooms(3) })

  const featuredPosts = useMemo(() => pickRecentPopularPosts(posts as Post[], 24), [posts])
  const upcomingEvents = useMemo(() => getCurrentEventWindow(allEvents as Event[]), [allEvents])
  const upcomingTicketing = useMemo(() => getCurrentTicketingWindow(ticketingEvents as Event[]), [ticketingEvents])
  const categoryNameById = useMemo(() => {
    return new Map((categories as any[]).map((category: any) => [category.id, category.name]))
  }, [categories])

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
          {upcomingTicketing.length ? (
            <div className="space-y-3">
              {upcomingTicketing.slice(0, 5).map((event) => (
                <TicketingRow key={event.id} event={event} />
              ))}
            </div>
          ) : (
            <EmptyTicketingState />
          )}
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
        <div className="flex items-end justify-between gap-3">
          <div>
            <h2 className="text-[18px] font-black tracking-tight text-slate-950">인기글</h2>
            <p className="mt-1 text-xs text-slate-500">24시간 내 인기글만 보여줘요.</p>
          </div>
          <Link to="/community" className="hidden text-sm font-medium text-[var(--accent)] md:inline">전체보기</Link>
        </div>
        <div className="mt-4 grid gap-3">
          {featuredPosts.length ? featuredPosts.map((post) => (
            <PostRow
              key={post.id}
              post={post}
              categoryLabel={categoryNameById.get(post.categoryId) ?? post.categoryName}
            />
          )) : (
            <div className="rounded-[20px] border border-dashed border-[var(--line)] bg-slate-50 px-4 py-8 text-center text-sm text-slate-500">
              아직 24시간 내 인기글이 없어요.
            </div>
          )}
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

function pickRecentPopularPosts(posts: Post[], hours = 24) {
  const cutoff = Date.now() - hours * 60 * 60 * 1000
  return posts
    .filter((post) => new Date(post.createdAt).getTime() >= cutoff)
    .sort((a, b) => (b.likeCount + b.commentCount) - (a.likeCount + a.commentCount))
    .slice(0, 4)
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
  return sortUpcomingEvents(events.filter((event) => isWithinDays(event.startAt, 7)))
}

function getCurrentTicketingWindow(events: Event[]) {
  return sortUpcomingTicketing(
    events.filter((event) => {
      if (!event.hasTicketing || !event.ticketingOpenAt) return false
      return isWithinDays(event.ticketingOpenAt, 7) || (isTicketingOpen(event) && isWithinDays(event.startAt, 7))
    })
  )
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
          <span className={`rounded-full px-2.5 py-1 text-[11px] font-semibold ${chipClass}`}>{displayEventCategoryLabel(event.categoryName)}</span>
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
        원하시는 일정이 있나요? <Link to="/community/new?requestKind=event" className="font-semibold text-[var(--accent)] underline decoration-[var(--accent-soft)] decoration-2 underline-offset-4 transition-colors hover:text-[var(--accent-dark)]">행사 요청하러 가기!</Link>
      </p>
    </div>
  )
}

function EmptyTicketingState() {
  return (
    <div className="rounded-[20px] border border-dashed border-[#dcd6f6] bg-white/70 p-5">
      <div className="text-sm font-semibold text-slate-900">열리는 티켓팅이 없네요!</div>
      <p className="mt-1 text-sm leading-6 text-slate-500">
        곧 열릴 티켓팅이 확인되면 여기서 바로 보여드릴게요.
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
          <div className={`inline-flex rounded-full px-2.5 py-1 text-[11px] font-semibold ${chipClass}`}>{displayEventCategoryLabel(event.categoryName)}</div>
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

function PostRow({ post, categoryLabel }: { post: Post; categoryLabel?: string }) {
  const label = categoryLabel ?? post.categoryName ?? '카테고리'
  const chipClass = postChipClass(label)
  return (
    <Link
      to={`/community/${post.id}`}
      className="flex items-center justify-between gap-4 rounded-[20px] border border-[var(--line)] bg-slate-50 px-5 py-4 hover:bg-white md:px-6"
    >
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <span className={`rounded-full px-2.5 py-1 text-[11px] font-semibold ${chipClass}`}>{label}</span>
          <div className="min-w-0 flex-1">
            <div className="truncate text-[15px] font-semibold leading-6 text-slate-950">{post.title}</div>
          </div>
        </div>
        <div className="mt-2 flex flex-wrap items-center gap-2 text-[12px] text-slate-500">
          {post.eventName && <span>{post.eventName}</span>}
          <span>{post.authorNickname ?? '익명'} · {post.createdAt}</span>
        </div>
      </div>
      <div className="shrink-0 text-right text-[12px] text-slate-500">
        <div>♡ {post.likeCount}</div>
        <div>💬 {post.commentCount}</div>
      </div>
    </Link>
  )
}

function categoryChipClass(name: string) {
  return (
    {
      concert: 'bg-violet-100 text-violet-700',
      festival: 'bg-fuchsia-100 text-fuchsia-700',
      fanmeeting: 'bg-pink-100 text-pink-700',
      popup: 'bg-sky-100 text-sky-700',
    }[normalizeCategoryKey(name)] ?? 'bg-violet-100 text-violet-700'
  )
}

function displayEventCategoryLabel(name: string) {
  const key = normalizeCategoryKey(name)
  return {
    concert: '콘서트',
    festival: '축제',
    fanmeeting: '팬미팅',
    popup: '팝업스토어',
  }[key] ?? (String(name ?? '').trim().toUpperCase() || 'EVENT')
}

function postChipClass(name: string) {
  return (
    {
      review: 'bg-violet-100 text-violet-700',
      tip: 'bg-sky-100 text-sky-700',
      free: 'bg-emerald-100 text-emerald-700',
      request: 'bg-rose-100 text-rose-700',
    }[normalizePostCategoryKey(name)] ?? 'bg-[var(--accent-soft)] text-[var(--accent)]'
  )
}

function normalizeCategoryKey(name: string) {
  const normalized = String(name ?? '').trim().toLowerCase()
  const compact = normalized.replace(/[\s_-]+/g, '')
  if (compact.includes('concert') || compact.includes('콘서트')) return 'concert'
  if (compact.includes('festival') || compact.includes('페스티벌') || compact.includes('축제')) return 'festival'
  if (compact.includes('fanmeeting') || compact.includes('팬미팅') || compact.includes('팬 미팅')) return 'fanmeeting'
  if (compact.includes('popup') || compact.includes('팝업스토어') || compact.includes('팝업') || compact.includes('pop-up') || compact.includes('pop up')) return 'popup'
  return normalized
}

function normalizePostCategoryKey(name: string) {
  const normalized = String(name ?? '').trim().toLowerCase()
  if (normalized === '후기' || normalized === 'review') return 'review'
  if (normalized === '꿀팁' || normalized === 'tip') return 'tip'
  if (normalized === '자유' || normalized === 'free') return 'free'
  if (normalized === '요청' || normalized === 'request') return 'request'
  return normalized
}
