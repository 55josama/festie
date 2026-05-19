import { useEffect, useMemo, useState, type ReactNode } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getCategories } from '../api/community'
import { getEvents, getTicketingEvents } from '../api/events'
import { getPosts } from '../api/community'
import { getPopularChatRooms } from '../api/chat'
import { getCalendars } from '../api/calendar'
import { formatDateRange } from '../lib/format'
import { useAuthStore } from '../store/authStore'
import { formatDateTime } from '../lib/format'
import { STATUS_LABEL, type CalendarEntry, type Event, type Post } from '../types'

export default function Home() {
  const navigate = useNavigate()
  const today = new Date()
  const [homeCalendarYear, setHomeCalendarYear] = useState(today.getFullYear())
  const [homeCalendarMonth, setHomeCalendarMonth] = useState(today.getMonth() + 1)
  const [selectedHomeDay, setSelectedHomeDay] = useState<number | null>(null)
  const { isLoggedIn } = useAuthStore()
  const { data: allEvents = [] } = useQuery({ queryKey: ['events', 'home'], queryFn: () => getEvents({ size: 100 }) })
  const { data: ticketingEvents = [] } = useQuery({ queryKey: ['events', 'ticketing'], queryFn: getTicketingEvents })
  const { data: myCalendars = [] } = useQuery({
    queryKey: ['calendars', 'home', homeCalendarYear, homeCalendarMonth],
    queryFn: () => getCalendars(homeCalendarYear, homeCalendarMonth),
    enabled: isLoggedIn(),
  })
  const { data: posts = [] } = useQuery({ queryKey: ['posts', 'home'], queryFn: () => getPosts({ size: 4, sort: 'createdAt,desc' }) })
  const { data: categories = [] } = useQuery({ queryKey: ['categories', 'home'], queryFn: getCategories })
  const { data: popularRooms = [] } = useQuery({ queryKey: ['popular-chat-rooms', 'home'], queryFn: () => getPopularChatRooms(3) })
  const homeEvents = useMemo(() => allEvents as Event[], [allEvents])
  const [upcomingPage, setUpcomingPage] = useState(0)
  const [ongoingPage, setOngoingPage] = useState(0)
  const [ticketingPage, setTicketingPage] = useState(0)
  const [mobileSearchQuery, setMobileSearchQuery] = useState('')
  const homeSectionPageSize = 5

  const featuredPosts = useMemo(() => pickRecentPopularPosts(posts as Post[], 24), [posts])
  const upcomingEvents = useMemo(() => getCurrentEventWindow(allEvents as Event[]), [allEvents])
  const ongoingEvents = useMemo(() => getCurrentOngoingEventWindow(allEvents as Event[]), [allEvents])
  const upcomingTicketing = useMemo(() => getCurrentTicketingWindow(ticketingEvents as Event[]), [ticketingEvents])
  const categoryNameById = useMemo(() => {
    return new Map((categories as any[]).map((category: any) => [category.id, category.name]))
  }, [categories])
  const mobileFeaturedEvents = useMemo(
    () => uniqueEventsById([...upcomingEvents, ...ongoingEvents, ...upcomingTicketing]).slice(0, 4),
    [ongoingEvents, upcomingEvents, upcomingTicketing],
  )
  const mobilePopularPosts = useMemo(() => featuredPosts.slice(0, 3), [featuredPosts])
  const mobilePopularRooms = useMemo(() => (popularRooms as any[]).slice(0, 3), [popularRooms])
  const homeCalendarItems = useMemo(() => {
    const eventById = new Map((allEvents as Event[]).map((event) => [event.id, event]))
    return [...(myCalendars as CalendarEntry[])]
      .sort((a, b) => String(a.eventDate ?? '').localeCompare(String(b.eventDate ?? '')))
      .map((item) => ({
        ...item,
        event: eventById.get(item.eventId) ?? null,
      }))
  }, [allEvents, myCalendars])
  const mobileMyCalendarItems = useMemo(() => homeCalendarItems.slice(0, 3), [homeCalendarItems])
  const upcomingPages = Math.max(1, Math.ceil(upcomingEvents.length / homeSectionPageSize))
  const ongoingPages = Math.max(1, Math.ceil(ongoingEvents.length / homeSectionPageSize))
  const ticketingPages = Math.max(1, Math.ceil(upcomingTicketing.length / homeSectionPageSize))
  const upcomingPageItems = useMemo(() => paginateItems(upcomingEvents, upcomingPage, homeSectionPageSize), [upcomingEvents, upcomingPage])
  const ongoingPageItems = useMemo(() => paginateItems(ongoingEvents, ongoingPage, homeSectionPageSize), [ongoingEvents, ongoingPage])
  const ticketingPageItems = useMemo(() => paginateItems(upcomingTicketing, ticketingPage, homeSectionPageSize), [upcomingTicketing, ticketingPage])

  useEffect(() => {
    if (upcomingPage > upcomingPages - 1) setUpcomingPage(Math.max(upcomingPages - 1, 0))
  }, [upcomingPage, upcomingPages])

  useEffect(() => {
    if (ongoingPage > ongoingPages - 1) setOngoingPage(Math.max(ongoingPages - 1, 0))
  }, [ongoingPage, ongoingPages])

  useEffect(() => {
    if (ticketingPage > ticketingPages - 1) setTicketingPage(Math.max(ticketingPages - 1, 0))
  }, [ticketingPage, ticketingPages])

  const submitMobileSearch = () => {
    const next = mobileSearchQuery.trim()
    if (!next) return
    navigate(`/events?query=${encodeURIComponent(next)}`)
  }

  const shiftHomeMonth = (offset: number) => {
    setSelectedHomeDay(null)
    const next = new Date(homeCalendarYear, homeCalendarMonth - 1 + offset, 1)
    setHomeCalendarYear(next.getFullYear())
    setHomeCalendarMonth(next.getMonth() + 1)
  }

  return (
    <div className="space-y-6 px-4 py-4 pb-24 lg:px-8 lg:py-7 lg:pb-7">
      <div className="space-y-5 lg:hidden">
        <section className="rounded-[32px] border border-[var(--line)] bg-white p-4 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
          <div className="space-y-1.5">
            <div className="text-[22px] font-black leading-tight tracking-tight text-slate-950">
              당신의 모든 특별한 순간을 연결하다
            </div>
            <div className="text-[12px] leading-5 text-slate-500">
              찾으시는 행사나 주제를 검색해보세요!
            </div>
          </div>

          <div className="mt-2 flex justify-end">
            <div className="inline-flex rounded-full bg-slate-100 px-3 py-1 text-[11px] font-medium text-slate-500">
              추천 검색어: 콘서트, 축제
            </div>
          </div>

          <label className="mt-4 flex items-center gap-2 rounded-full border border-[var(--line)] bg-slate-50 px-4 py-3 shadow-[inset_0_1px_0_rgba(255,255,255,0.75)]">
            <span className="text-[16px] text-slate-400">⌕</span>
            <input
              value={mobileSearchQuery}
              onChange={(e) => setMobileSearchQuery(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  e.preventDefault()
                  submitMobileSearch()
                }
              }}
              placeholder="행사, 아티스트, 장소 검색"
              className="min-w-0 flex-1 bg-transparent text-sm outline-none placeholder:text-slate-400"
            />
            <button
              type="button"
              onClick={submitMobileSearch}
              className="rounded-full bg-[var(--accent-soft)] px-3 py-2 text-xs font-semibold text-[var(--accent)]"
            >
              검색
            </button>
          </label>
        </section>

        <section className="space-y-3">
          <div className="flex items-center justify-between gap-3">
            <div>
              <h2 className="text-[18px] font-black tracking-tight text-slate-950">추천 행사</h2>
              <p className="mt-1 text-xs text-slate-500">지금 가장 먼저 볼 행사들이에요.</p>
            </div>
            <Link to="/events" className="text-sm font-medium text-[var(--accent)]">
              전체보기
            </Link>
          </div>
          <div className="flex gap-3 overflow-x-scroll pb-4 festie-horizontal-scroll">
            {mobileFeaturedEvents.length ? (
              mobileFeaturedEvents.map((event) => (
                <FeaturedEventCard key={event.id} event={event} variant="compact" />
              ))
            ) : (
              <div className="w-full rounded-[24px] border border-dashed border-[var(--line)] bg-white px-4 py-8 text-center text-sm text-slate-500">
                아직 보여줄 행사가 없어요.
              </div>
            )}
          </div>
        </section>

        <section className="rounded-[28px] border border-[var(--line)] bg-white p-4 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
          <div className="flex items-center justify-between gap-3">
            <div>
              <h2 className="text-[18px] font-black tracking-tight text-slate-950">내 일정</h2>
              <p className="mt-1 text-xs text-slate-500">내가 추가한 나의 캘린더를 먼저 보여줘요.</p>
            </div>
            <Link to="/my/calendars" className="text-sm font-medium text-[var(--accent)]">
              내 캘린더
            </Link>
          </div>
          <div className="mt-4 space-y-3">
            {isLoggedIn() ? (
              mobileMyCalendarItems.length ? (
                mobileMyCalendarItems.map((item) => (
                  <MobileCalendarRow key={item.id} item={item} />
                ))
              ) : (
                <div className="rounded-[20px] border border-dashed border-[var(--line)] bg-slate-50 px-4 py-6 text-center text-sm text-slate-500">
                  아직 추가한 일정이 없어요.
                </div>
              )
            ) : (
              <div className="rounded-[20px] border border-dashed border-[var(--line)] bg-slate-50 px-4 py-6 text-center text-sm text-slate-500">
                로그인하면 내가 추가한 일정이 보여요.
              </div>
            )}
          </div>
        </section>

        <section className="rounded-[28px] border border-[var(--line)] bg-white p-4 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
          <div className="flex items-center justify-between gap-3">
            <div>
              <h2 className="text-[18px] font-black tracking-tight text-slate-950">실시간 커뮤니티</h2>
              <p className="mt-1 text-xs text-slate-500">인기글을 빠르게 훑어봐요.</p>
            </div>
            <Link to="/community" className="text-sm font-medium text-[var(--accent)]">
              더 보기
            </Link>
          </div>
          <div className="mt-4 space-y-3">
            {mobilePopularPosts.length ? (
              mobilePopularPosts.map((post) => (
                <PostRow
                  key={post.id}
                  post={post}
                  categoryLabel={categoryNameById.get(post.categoryId) ?? post.categoryName}
                />
              ))
            ) : (
              <div className="rounded-[20px] border border-dashed border-[var(--line)] bg-slate-50 px-4 py-6 text-center text-sm text-slate-500">
                아직 인기글이 없어요.
              </div>
            )}
          </div>
        </section>

        <section className="rounded-[28px] border border-[var(--line)] bg-white p-4 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
          <div className="flex items-center justify-between gap-3">
            <div>
              <h2 className="text-[18px] font-black tracking-tight text-slate-950">실시간 인기 채팅방</h2>
              <p className="mt-1 text-xs text-slate-500">지금 사람들이 많이 보고 있어요.</p>
            </div>
            <Link to="/events" className="text-sm font-medium text-[var(--accent)]">
              이동
            </Link>
          </div>
          <div className="mt-4 space-y-3">
            {mobilePopularRooms.length ? (
              mobilePopularRooms.map((room) => (
                <Link
                  key={room.chatRoomId}
                  to={`/events/${room.eventId}`}
                  className="block rounded-[20px] border border-[var(--line)] bg-slate-50 px-4 py-3"
                >
                  <div className="flex items-center justify-between gap-3">
                    <div className="min-w-0">
                      <div className="text-[11px] font-semibold text-[var(--accent)]">{room.category}</div>
                      <div className="mt-1 truncate text-sm font-semibold text-slate-950">{room.eventName}</div>
                    </div>
                    <div className="shrink-0 rounded-full bg-white px-3 py-1 text-xs font-semibold text-slate-600">
                      {room.currentViewerCount ?? 0}명
                    </div>
                  </div>
                </Link>
              ))
            ) : (
              <div className="rounded-[20px] border border-dashed border-[var(--line)] bg-slate-50 px-4 py-6 text-center text-sm text-slate-500">
                아직 인기 채팅방이 없어요.
              </div>
            )}
          </div>
        </section>
      </div>

      <div className="hidden lg:block">
        <div className="grid gap-6 lg:grid-cols-[minmax(0,8.2fr)_minmax(280px,1.8fr)]">
          <div className="min-w-0 space-y-6">
            <section className="rounded-[24px] border border-[var(--line)] bg-white p-4 shadow-[0_12px_30px_rgba(15,23,42,0.04)] lg:p-4">
              <SectionHeading
                title="당신의 모든 특별한 순간을 연결하다, Festie"
              />
              <p className="mt-2 text-sm text-slate-500">
                특별한 하루를 놓치지 않도록, 중요한 소식과 추천 행사를 한곳에 담았어요.
              </p>
              <div className="mt-4 flex gap-4 overflow-x-scroll pb-4 festie-horizontal-scroll">
                <NoticePromoCard />
                {mobileFeaturedEvents.length ? (
                  mobileFeaturedEvents.map((event) => (
                    <FeaturedEventCard key={event.id} event={event} variant="compact" />
                  ))
                ) : (
                  <div className="flex min-h-[420px] w-[72vw] max-w-[290px] shrink-0 items-center justify-center rounded-[22px] border border-dashed border-[var(--line)] bg-slate-50 px-4 py-8 text-center text-sm text-slate-500">
                    아직 보여줄 행사가 없어요.
                  </div>
                )}
              </div>
            </section>

            <section className="grid gap-6 lg:grid-cols-3">
              <div className="min-w-0 space-y-4 rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
                <SectionHeading
                  title="다가오는 행사"
                  action={<Link to="/calendar" className="text-sm font-medium text-[var(--accent)]">캘린더</Link>}
                />
                {upcomingPageItems.length ? (
                  <div className="space-y-3">
                    {upcomingPageItems.map((event) => (
                      <CompactEventRow key={event.id} event={event} />
                    ))}
                  </div>
                ) : (
                  <EmptyEventState />
                )}
                {upcomingEvents.length > homeSectionPageSize && (
                  <SectionPager
                    page={upcomingPage}
                    totalPages={upcomingPages}
                    onPrev={() => setUpcomingPage((prev) => Math.max(prev - 1, 0))}
                    onNext={() => setUpcomingPage((prev) => Math.min(prev + 1, upcomingPages - 1))}
                  />
                )}
              </div>

              <div className="min-w-0 space-y-4 rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
                <SectionHeading
                  title="진행중인 행사"
                  action={<Link to="/calendar?status=IN_PROGRESS" className="text-sm font-medium text-[var(--accent)]">더 보기</Link>}
                />
                {ongoingPageItems.length ? (
                  <div className="space-y-3">
                    {ongoingPageItems.map((event) => (
                      <CompactEventRow key={event.id} event={event} />
                    ))}
                  </div>
                ) : (
                  <EmptyOngoingState />
                )}
                {ongoingEvents.length > homeSectionPageSize && (
                  <SectionPager
                    page={ongoingPage}
                    totalPages={ongoingPages}
                    onPrev={() => setOngoingPage((prev) => Math.max(prev - 1, 0))}
                    onNext={() => setOngoingPage((prev) => Math.min(prev + 1, ongoingPages - 1))}
                  />
                )}
              </div>

              <div className="min-w-0 space-y-4 rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
                <SectionHeading
                  title="곧 열리는 티켓팅"
                  action={<Link to="/calendar" className="text-sm font-medium text-[var(--accent)]">더 보기</Link>}
                />
                {ticketingPageItems.length ? (
                  <div className="space-y-3">
                    {ticketingPageItems.map((event) => (
                      <TicketingRow key={event.id} event={event} />
                    ))}
                  </div>
                ) : (
                  <EmptyTicketingState />
                )}
                {upcomingTicketing.length > homeSectionPageSize && (
                  <SectionPager
                    page={ticketingPage}
                    totalPages={ticketingPages}
                    onPrev={() => setTicketingPage((prev) => Math.max(prev - 1, 0))}
                    onNext={() => setTicketingPage((prev) => Math.min(prev + 1, ticketingPages - 1))}
                  />
                )}
              </div>
            </section>
          </div>

          <aside className="min-w-0 space-y-6">
            <section className="rounded-[24px] border border-[#d9cfff] bg-[#f5efff] p-4 shadow-[0_12px_30px_rgba(15,23,42,0.04)] lg:p-5">
              <div className="mt-4">
                <HomeEventCalendar
                  year={homeCalendarYear}
                  month={homeCalendarMonth}
                  events={homeEvents}
                  selectedDay={selectedHomeDay}
                  onSelectDay={setSelectedHomeDay}
                  onPrevMonth={() => shiftHomeMonth(-1)}
                  onNextMonth={() => shiftHomeMonth(1)}
                />
              </div>
            </section>

            <section className="rounded-[24px] border border-[#d9cfff] bg-[#f5efff] p-3 shadow-[0_12px_30px_rgba(15,23,42,0.04)] lg:p-4">
              <SectionHeading
                title="실시간 인기 채팅방"
                action={<Link to="/events" className="text-sm font-medium text-[var(--accent)]">이동</Link>}
              />
              <div className="mt-3 space-y-2">
                {(popularRooms as any[]).length ? (
                  (popularRooms as any[]).map((room: any) => (
                    <Link
                      key={room.chatRoomId}
                      to={`/events/${room.eventId}`}
                      className="block rounded-[18px] border border-white/80 bg-white/70 px-3 py-2.5 hover:bg-white"
                    >
                      <div className="text-[11px] font-semibold text-[var(--accent)]">{room.category}</div>
                      <div className="mt-0.5 truncate text-[13px] font-semibold text-slate-950">{room.eventName}</div>
                      <div className="mt-1 flex items-center justify-between text-[10px] text-slate-500">
                        <span>{room.status}</span>
                        <span>{room.currentViewerCount ?? 0}명</span>
                      </div>
                    </Link>
                  ))
                ) : (
                  <div className="rounded-[20px] border border-dashed border-white/80 bg-white/60 px-3 py-5 text-center text-sm text-slate-500">
                    아직 인기 채팅방이 없어요.
                  </div>
                )}
              </div>
            </section>

            <section className="rounded-[24px] border border-[#d9cfff] bg-[#f5efff] p-3 shadow-[0_12px_30px_rgba(15,23,42,0.04)] lg:p-4">
              <div className="flex items-end justify-between gap-3">
                <div>
                  <h2 className="text-[18px] font-black tracking-tight text-slate-950">24시간 인기글</h2>
                  <p className="mt-1 text-xs text-slate-500">최근 반응이 좋은 글만 보여줘요.</p>
                </div>
                <Link to="/community" className="text-sm font-medium text-[var(--accent)]">전체보기</Link>
              </div>
              <div className="mt-3 space-y-2.5">
                {featuredPosts.length ? featuredPosts.map((post) => (
                  <PostRow
                    key={post.id}
                    post={post}
                    categoryLabel={categoryNameById.get(post.categoryId) ?? post.categoryName}
                  />
                )) : (
                  <div className="rounded-[20px] border border-dashed border-white/80 bg-white/60 px-3 py-5 text-center text-sm text-slate-500">
                    아직 24시간 내 인기글이 없어요.
                  </div>
                )}
              </div>
            </section>
          </aside>
        </div>
      </div>
    </div>
  )
}

function sortUpcomingEvents(events: Event[]) {
  return [...events]
    .filter((event) => isUpcomingEvent(event))
    .sort((a, b) => String(a.startAt ?? '').localeCompare(String(b.startAt ?? '')))
}

function sortUpcomingTicketing(events: Event[]) {
  return [...events]
    .filter((event) => event.hasTicketing)
    .filter((event) => isUpcomingEvent(event))
    .sort((a, b) => String(a.ticketingOpenAt ?? '').localeCompare(String(b.ticketingOpenAt ?? '')))
}

function pickRecentPopularPosts(posts: Post[], hours = 24) {
  const cutoff = Date.now() - hours * 60 * 60 * 1000
  return posts
    .filter((post) => new Date(post.createdAt).getTime() >= cutoff)
    .sort((a, b) => (b.likeCount + b.commentCount) - (a.likeCount + a.commentCount))
    .slice(0, 4)
}

function paginateItems<T>(items: T[], page: number, pageSize: number) {
  const safePage = Math.max(0, page)
  const start = safePage * pageSize
  return items.slice(start, start + pageSize)
}

function uniqueEventsById(events: Event[]) {
  const seen = new Set<string>()
  return events.filter((event) => {
    if (seen.has(event.id)) return false
    seen.add(event.id)
    return true
  })
}

function countdownLabel(dateValue?: string | null, mode: 'event' | 'ticketing' = 'event') {
  if (!dateValue) return mode === 'ticketing' ? '미정' : '진행중'
  const diffDays = dayDiffFromToday(dateValue)
  if (diffDays > 0) return `D-${diffDays}`
  if (diffDays === 0) return mode === 'ticketing' ? '오픈중' : 'D-DAY'
  return mode === 'ticketing' ? '오픈중' : '진행중'
}

function startOfToday() {
  const today = new Date()
  return new Date(today.getFullYear(), today.getMonth(), today.getDate())
}

function getCurrentEventWindow(events: Event[]) {
  return sortUpcomingEvents(events.filter((event) => isWithinDays(event.startAt, 7)))
}

function getCurrentOngoingEventWindow(events: Event[]) {
  return [...events]
    .filter((event) => event.status === 'IN_PROGRESS')
    .sort((a, b) => {
      const endDiff = String(a.endAt ?? '').localeCompare(String(b.endAt ?? ''))
      if (endDiff !== 0) return endDiff
      return String(a.startAt ?? '').localeCompare(String(b.startAt ?? ''))
    })
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
  const diffDays = dayDiffFromToday(dateValue)
  return diffDays >= 0 && diffDays <= limit
}

function dayDiffFromToday(dateValue: string) {
  const target = new Date(dateValue)
  if (Number.isNaN(target.getTime())) return -1
  const targetStart = new Date(target.getFullYear(), target.getMonth(), target.getDate())
  return Math.floor((targetStart.getTime() - startOfToday().getTime()) / 86400000)
}

function isUpcomingEvent(event: Event) {
  return event.status !== 'COMPLETED' && event.status !== 'CANCELLED'
}

function SectionHeading({ title, action }: { title: string; action?: ReactNode }) {
  return (
    <div className="flex items-center justify-between">
      <h2 className="text-[18px] font-black tracking-tight text-slate-950">{title}</h2>
      {action}
    </div>
  )
}

function SectionPager({
  page,
  totalPages,
  onPrev,
  onNext,
}: {
  page: number
  totalPages: number
  onPrev: () => void
  onNext: () => void
}) {
  return (
    <div className="flex items-center justify-center gap-2 pt-1">
      <button
        type="button"
        onClick={onPrev}
        disabled={page <= 0}
        className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:opacity-40"
      >
        이전
      </button>
      <div className="text-[11px] font-semibold text-slate-500">
        {page + 1} / {totalPages}
      </div>
      <button
        type="button"
        onClick={onNext}
        disabled={page >= totalPages - 1}
        className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:opacity-40"
      >
        다음
      </button>
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
          <span className="text-[11px] text-slate-500">{STATUS_LABEL[event.status] ?? event.status}</span>
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

function EmptyOngoingState() {
  return (
    <div className="rounded-[20px] border border-dashed border-[#dcd6f6] bg-white/70 p-5">
      <div className="text-sm font-semibold text-slate-900">진행중인 행사가 없네요!</div>
      <p className="mt-1 text-sm leading-6 text-slate-500">
        현재 열려 있는 행사가 생기면 여기서 바로 보여드릴게요.
      </p>
    </div>
  )
}

function FeaturedEventCard({
  event,
  variant = 'full',
}: {
  event: Event
  variant?: 'compact' | 'full'
}) {
  const chipClass = categoryChipClass(event.categoryName)
  const compactClass = variant === 'compact' ? 'w-[72vw] max-w-[290px] shrink-0' : 'w-full'
  return (
    <Link
      to={`/events/${event.id}`}
      className={`overflow-hidden rounded-[22px] border border-[var(--line)] bg-white shadow-[0_12px_28px_rgba(15,23,42,0.06)] ${compactClass}`}
    >
      <div className="relative aspect-[4/5] overflow-hidden bg-slate-100">
        {event.img ? (
          <img src={event.img} alt={event.name} className="h-full w-full object-cover" />
        ) : (
          <div className="absolute inset-0 bg-gradient-to-br from-[#d7ccff] via-[#efe9ff] to-[#ffe4f0]" />
        )}
        <div className="absolute inset-0 bg-[linear-gradient(180deg,rgba(15,23,42,0)_36%,rgba(15,23,42,0.68)_100%)]" />
        <div className="absolute inset-x-3 top-3 flex items-center justify-between gap-2">
          <span className={`rounded-full px-2.5 py-1 text-[10px] font-semibold ${chipClass}`}>{displayEventCategoryLabel(event.categoryName)}</span>
          <span className="rounded-full bg-white/90 px-2.5 py-1 text-[10px] font-semibold text-slate-700">
            {STATUS_LABEL[event.status] ?? event.status}
          </span>
        </div>
        <div className="absolute inset-x-3 bottom-3 space-y-1 text-white">
          <div className="text-[15px] font-black leading-5 tracking-tight drop-shadow-[0_1px_6px_rgba(15,23,42,0.55)]">
            {event.name}
          </div>
          <div className="text-[11px] leading-4 text-white/90 drop-shadow-[0_1px_4px_rgba(15,23,42,0.4)]">
            {formatDateRange(event.startAt, event.endAt)}
          </div>
          <div className="truncate text-[11px] leading-4 text-white/80 drop-shadow-[0_1px_4px_rgba(15,23,42,0.35)]">
            {event.place}
          </div>
        </div>
      </div>
    </Link>
  )
}

function NoticePromoCard() {
  return (
    <Link
      to="/notices"
      className="w-[72vw] max-w-[290px] shrink-0 overflow-hidden rounded-[22px] border border-[var(--line)] bg-white shadow-[0_12px_28px_rgba(15,23,42,0.06)]"
    >
      <div className="relative aspect-[4/5] overflow-hidden bg-[linear-gradient(180deg,#ffffff_0%,#faf7ff_44%,#f1ebff_100%)] p-[1px]">
        <div className="absolute inset-0 rounded-[21px] border border-white/80 bg-white/5" />
        <img
          src="/banner-notice.png"
          alt="공지사항"
          className="absolute inset-0 h-full w-full rounded-[21px] object-cover"
        />
        <div className="pointer-events-none absolute inset-0 rounded-[21px] bg-[linear-gradient(180deg,rgba(255,255,255,0.10)_0%,rgba(255,255,255,0.04)_52%,rgba(255,255,255,0.12)_100%)]" />
        <div className="pointer-events-none absolute inset-[1px] rounded-[20px] border border-white/65" />
        <div className="absolute inset-x-4 bottom-4 space-y-2 text-slate-950">
          <div className="inline-flex rounded-full bg-white/90 px-2.5 py-1 text-[10px] font-semibold text-[var(--accent)] shadow-[0_4px_14px_rgba(15,23,42,0.05)]">
            공지사항
          </div>
          <div className="text-[17px] font-black leading-6 tracking-tight">
            먼저 확인하면 좋은 안내들을 모아뒀어요
          </div>
          <div className="text-[11px] leading-4 text-slate-600">
            운영 공지, 이벤트 소식, 중요한 변경사항을 바로 확인해보세요.
          </div>
        </div>
      </div>
    </Link>
  )
}

function HomeEventCalendar({
  year,
  month,
  events,
  selectedDay,
  onSelectDay,
  onPrevMonth,
  onNextMonth,
}: {
  year: number
  month: number
  events: Event[]
  selectedDay: number | null
  onSelectDay: (day: number) => void
  onPrevMonth: () => void
  onNextMonth: () => void
}) {
  const cells = buildMonthCells(year, month)
  const currentMonthLabel = new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'long',
  }).format(new Date(year, month - 1, 1))
  const today = new Date()
  const isCurrentMonth = today.getFullYear() === year && today.getMonth() + 1 === month
  const itemCountByDay = new Map<number, number>()
  const selectedItems = selectedDay
    ? events.filter((event) => {
        const dayStart = new Date(year, month - 1, selectedDay)
        const start = new Date(event.startAt)
        const end = new Date(event.endAt ?? event.startAt)
        if (Number.isNaN(start.getTime())) return false
        const startDay = startOfDay(start)
        const endDay = startOfDay(end)
        return dayStart >= startDay && dayStart <= endDay
      })
    : []

  events.forEach((event) => {
    const start = new Date(event.startAt)
    const end = new Date(event.endAt ?? event.startAt)
    if (Number.isNaN(start.getTime())) return
    const cursor = new Date(year, month - 1, 1)
    const monthEnd = new Date(year, month, 0)
    const startDay = startOfDay(start)
    const endDay = startOfDay(end)
    while (cursor <= monthEnd) {
      const dayKey = cursor.getDate()
      const isWithinRange = cursor >= startDay && cursor <= endDay
      if (isWithinRange) {
        itemCountByDay.set(dayKey, (itemCountByDay.get(dayKey) ?? 0) + 1)
      }
      cursor.setDate(cursor.getDate() + 1)
    }
  })

  return (
    <div>
      <div className="flex items-end justify-between gap-3">
        <div>
          <div className="text-[11px] font-semibold text-[var(--accent)]">이번 달</div>
          <div className="text-[18px] font-black tracking-tight text-slate-950">{currentMonthLabel}</div>
        </div>
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={onPrevMonth}
            className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-[11px] font-semibold text-slate-700"
          >
            ‹
          </button>
          <button
            type="button"
            onClick={onNextMonth}
            className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-[11px] font-semibold text-slate-700"
          >
            ›
          </button>
        </div>
      </div>

      <div className="mt-4 grid grid-cols-7 gap-1 text-center text-[10px] font-semibold text-slate-400">
        {['일', '월', '화', '수', '목', '금', '토'].map((day) => (
          <div key={day} className="py-1">
            {day}
          </div>
        ))}
      </div>

      <div className="mt-2 grid grid-cols-7 gap-1">
        {cells.map((day, index) => {
          if (!day) {
            return <div key={`blank-${index}`} className="h-11 rounded-[12px] bg-slate-50/70" />
          }

          const isToday = isCurrentMonth && today.getDate() === day
          const count = itemCountByDay.get(day) ?? 0

          const isSelected = selectedDay === day

          return (
            <button
              key={day}
              type="button"
              onClick={() => onSelectDay(day)}
              className={`flex h-11 flex-col items-center justify-center rounded-[12px] border text-[11px] font-semibold transition-colors ${
                isSelected
                  ? 'border-[var(--accent)] bg-[var(--accent)] text-white'
                  : isToday
                    ? 'border-[var(--accent)] bg-[var(--accent-soft)] text-[var(--accent)]'
                    : 'border-[var(--line)] bg-white text-slate-700 hover:bg-slate-50'
              }`}
            >
              <span>{day}</span>
              {count > 0 ? (
                <span className={`mt-0.5 text-[9px] font-bold ${isSelected ? 'text-white/90' : 'text-[var(--accent)]'}`}>{count}</span>
              ) : null}
            </button>
          )
        })}
      </div>

      {selectedDay ? (
        <div className="mt-4 space-y-2">
          {selectedItems.length ? (
            selectedItems.map((event) => (
              <Link key={event.id} to={`/events/${event.id}`} className="flex items-center justify-between gap-3 rounded-[16px] bg-slate-50 px-3 py-2 hover:bg-white">
                <div className="min-w-0">
                  <div className="truncate text-sm font-semibold text-slate-950">{event.name}</div>
                  <div className="text-[11px] text-slate-500">{formatDateRange(event.startAt, event.endAt)}</div>
                </div>
                <span className="shrink-0 text-[11px] font-semibold text-[var(--accent)]">보기</span>
              </Link>
            ))
          ) : (
            <div className="rounded-[18px] border border-dashed border-[var(--line)] bg-slate-50 px-4 py-6 text-center text-sm text-slate-500">
              이 날짜에 추가한 일정이 없어요.
            </div>
          )}
        </div>
      ) : (
          <div className="mt-4 rounded-[18px] border border-dashed border-[var(--line)] bg-slate-50 px-4 py-6 text-center text-sm text-slate-500">
          날짜를 클릭하면 해당 행사가 보여요.
        </div>
      )}
    </div>
  )
}

function MobileCalendarRow({
  item,
}: {
  item: CalendarEntry & { event: Event | null }
}) {
  const categoryLabel = item.event ? displayEventCategoryLabel(item.event.categoryName) : '일정'
  const memo = String(item.memo ?? '').trim()

  return (
    <Link
      to={item.eventId ? `/events/${item.eventId}` : '/my/calendars'}
      className="flex items-center gap-3 rounded-[20px] border border-[var(--line)] bg-slate-50 px-3 py-3 transition-colors hover:bg-white"
    >
      <div className="flex h-14 w-14 shrink-0 overflow-hidden rounded-[16px] bg-[var(--accent-soft)]">
        {item.event?.img ? (
          <img src={item.event.img} alt={item.eventName} className="h-full w-full object-cover" />
        ) : (
          <div className="flex h-full w-full items-center justify-center text-center text-[10px] font-semibold text-[var(--accent)]">
            {item.eventName.slice(0, 4) || '일정'}
          </div>
        )}
      </div>
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <span className="rounded-full bg-white px-2 py-1 text-[10px] font-semibold text-slate-500">{categoryLabel}</span>
          <span className="text-[10px] text-slate-400">{formatDateTime(item.eventDate)}</span>
        </div>
        <div className="mt-1 truncate text-sm font-semibold text-slate-950">{item.eventName}</div>
        <div className="mt-0.5 truncate text-[11px] text-slate-500">{memo || '메모 없음'}</div>
      </div>
    </Link>
  )
}

function startOfDay(date: Date) {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate())
}

function TicketingRow({ event }: { event: Event }) {
  const chipClass = categoryChipClass(event.categoryName)
  return (
    <Link
      to={`/events/${event.id}`}
      className="flex items-center justify-between gap-4 rounded-[18px] border border-[var(--line)] bg-slate-50 px-4 py-3 hover:bg-white"
    >
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <span className={`rounded-full px-2.5 py-1 text-[11px] font-semibold ${chipClass}`}>{displayEventCategoryLabel(event.categoryName)}</span>
          <span className="text-[11px] text-slate-500">티켓팅</span>
        </div>
        <div className="mt-2 truncate text-sm font-semibold text-slate-950">{event.name}</div>
        <div className="mt-1 truncate text-xs text-slate-500">
          {formatDateRange(event.startAt, event.endAt)} · {event.place}
        </div>
      </div>
      <div className="shrink-0 text-right">
        <div className="text-xs font-semibold text-[var(--accent)]">{countdownLabel(event.ticketingOpenAt, 'ticketing')}</div>
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
      className="flex items-center justify-between gap-4 rounded-[20px] border border-[var(--line)] bg-slate-50 px-5 py-4 hover:bg-white lg:px-6"
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

function buildMonthCells(year: number, month: number) {
  const firstDay = new Date(year, month - 1, 1).getDay()
  const lastDate = new Date(year, month, 0).getDate()
  const cells: Array<number | null> = []

  for (let index = 0; index < firstDay; index += 1) {
    cells.push(null)
  }
  for (let day = 1; day <= lastDate; day += 1) {
    cells.push(day)
  }
  while (cells.length % 7 !== 0) {
    cells.push(null)
  }

  return cells
}
