import { useEffect, useMemo, useState, type ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getCategories } from '../api/community'
import { getEvents, getTicketingEvents } from '../api/events'
import { getPosts } from '../api/community'
import { getPopularChatRooms } from '../api/chat'
import { formatDateRange } from '../lib/format'
import { useAuthStore } from '../store/authStore'
import type { Event, Post } from '../types'

type HomeBanner = {
  id: string
  title: string
  link: string
  imageUrl: string
  eventId?: string
}

const HOME_BANNER_STORAGE_KEY = 'festie-home-banners'
const DEFAULT_HOME_BANNERS: HomeBanner[] = [
  {
    id: 'banner-events',
    title: '행사 바로가기',
    link: '/events',
    imageUrl: '',
  },
  {
    id: 'banner-requests',
    title: '요청 글',
    link: '/community?tab=requests&requestKind=event',
    imageUrl: '',
  },
  {
    id: 'banner-popular',
    title: '인기글',
    link: '/community?tab=posts',
    imageUrl: '',
  },
]

export default function Home() {
  const { user } = useAuthStore()
  const { data: allEvents = [] } = useQuery({ queryKey: ['events', 'home'], queryFn: () => getEvents({ size: 100 }) })
  const { data: ticketingEvents = [] } = useQuery({ queryKey: ['events', 'ticketing'], queryFn: getTicketingEvents })
  const { data: posts = [] } = useQuery({ queryKey: ['posts', 'home'], queryFn: () => getPosts({ size: 4, sort: 'createdAt,desc' }) })
  const { data: categories = [] } = useQuery({ queryKey: ['categories', 'home'], queryFn: getCategories })
  const { data: popularRooms = [] } = useQuery({ queryKey: ['popular-chat-rooms', 'home'], queryFn: () => getPopularChatRooms(3) })
  const isAdmin = !!user && String(user.role ?? '').includes('ADMIN')
  const [homeBanners, setHomeBanners] = useState<HomeBanner[]>(DEFAULT_HOME_BANNERS)
  const [isBannerEditorOpen, setIsBannerEditorOpen] = useState(false)
  const [bannerDrafts, setBannerDrafts] = useState<HomeBanner[]>(DEFAULT_HOME_BANNERS)
  const [hasHydratedBanners, setHasHydratedBanners] = useState(false)

  useEffect(() => {
    if (typeof window === 'undefined') return
    try {
      const stored = window.localStorage.getItem(HOME_BANNER_STORAGE_KEY)
      if (!stored) return
      const parsed = JSON.parse(stored) as HomeBanner[]
      if (Array.isArray(parsed) && parsed.length) {
        const normalized = parsed.slice(0, 3).map((banner, index) => ({
          id: banner.id || `banner-${index}`,
          title: String(banner.title ?? '').trim(),
          link: String(banner.link ?? '').trim(),
          imageUrl: String(banner.imageUrl ?? '').trim(),
          eventId: String((banner as HomeBanner).eventId ?? '').trim(),
        }))
        const fallback = DEFAULT_HOME_BANNERS.slice(0, 3)
        setHomeBanners(normalized.length ? normalized : fallback)
        setBannerDrafts(normalized.length ? normalized : fallback)
      }
    } catch {
      const fallback = DEFAULT_HOME_BANNERS.slice(0, 3)
      setHomeBanners(fallback)
      setBannerDrafts(fallback)
    }
    setHasHydratedBanners(true)
  }, [])

  useEffect(() => {
    if (!hasHydratedBanners) return
    if (typeof window === 'undefined') return
    try {
      window.localStorage.setItem(HOME_BANNER_STORAGE_KEY, JSON.stringify(homeBanners))
    } catch {
      // ignore storage failures
    }
  }, [hasHydratedBanners, homeBanners])

  useEffect(() => {
    if (!hasHydratedBanners) return
    if (!allEvents.length) return

    let hasNextBannerChange = false
    const nextBanners = homeBanners.map((banner) => {
      if (banner.imageUrl.trim()) return banner
      if (banner.eventId?.trim()) {
        const eventImageUrl = suggestBannerImageUrl(`/events/${banner.eventId.trim()}`, allEvents as Event[])
        if (eventImageUrl) {
          hasNextBannerChange = true
          return { ...banner, imageUrl: eventImageUrl }
        }
      }
      const suggestedImageUrl = suggestBannerImageUrl(banner.link, allEvents as Event[])
      if (!suggestedImageUrl) return banner
      hasNextBannerChange = true
      return { ...banner, imageUrl: suggestedImageUrl }
    })

    if (hasNextBannerChange) {
      setHomeBanners(nextBanners)
      setBannerDrafts(nextBanners)
    }
  }, [allEvents, hasHydratedBanners, homeBanners])

  const featuredPosts = useMemo(() => pickRecentPopularPosts(posts as Post[], 24), [posts])
  const upcomingEvents = useMemo(() => getCurrentEventWindow(allEvents as Event[]), [allEvents])
  const upcomingTicketing = useMemo(() => getCurrentTicketingWindow(ticketingEvents as Event[]), [ticketingEvents])
  const categoryNameById = useMemo(() => {
    return new Map((categories as any[]).map((category: any) => [category.id, category.name]))
  }, [categories])

  return (
    <div className="space-y-6 px-5 py-5 md:px-8 md:py-7">
      <section className="rounded-[24px] border border-[var(--line)] bg-white p-4 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-6">
        <div className="flex items-start justify-between gap-3">
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
          {isAdmin && (
            <button
              type="button"
              onClick={() => {
                setBannerDrafts((homeBanners.length ? homeBanners : DEFAULT_HOME_BANNERS).slice(0, 3))
                setIsBannerEditorOpen(true)
              }}
              className="inline-flex h-8 w-8 items-center justify-center rounded-full border border-[var(--line)] bg-white text-[18px] font-bold text-slate-600 hover:bg-slate-50"
              aria-label="배너 편집"
            >
              +
            </button>
          )}
        </div>
        <div className="mt-5 hidden space-y-3 md:block">
          <div className="flex items-center justify-between gap-3">
            <span className="text-[11px] text-slate-400">행사 바로가기와 공지를 여기에 담아요</span>
          </div>
          <div className="grid gap-2 md:grid-cols-3">
            {homeBanners.slice(0, 3).map((banner, index) => (
              <BannerCard key={banner.id || `${banner.title}-${index}`} banner={banner} />
            ))}
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

      {isBannerEditorOpen && isAdmin && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 px-4 py-6">
          <div className="w-full max-w-5xl rounded-[28px] bg-white p-5 shadow-[0_20px_60px_rgba(15,23,42,0.18)] md:p-6">
            <div className="flex items-start justify-between gap-3">
              <div>
              <div className="inline-flex rounded-full bg-[var(--accent-soft)] px-3 py-1 text-[11px] font-semibold text-[var(--accent)]">배너 편집</div>
                <h3 className="mt-3 text-[22px] font-black tracking-tight text-slate-950">홈 배너 3개를 관리해요</h3>
                <p className="mt-1 text-xs text-slate-500">행사 UUID를 넣으면 링크와 이미지를 자동으로 채워요.</p>
                <p className="mt-1 text-xs font-semibold text-[var(--accent)]">권장 이미지: 1200 x 675px 이상, 16:9 비율</p>
              </div>
              <button
                type="button"
                onClick={() => setIsBannerEditorOpen(false)}
                className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-600"
              >
                닫기
              </button>
            </div>

            <div className="mt-5 grid gap-3">
              {bannerDrafts.slice(0, 3).map((banner, index) => (
                <div key={banner.id} className="rounded-[22px] border border-[var(--line)] bg-slate-50 p-4">
                  <div className="mb-3 flex items-center justify-between">
                    <div className="text-sm font-semibold text-slate-700">배너 {index + 1}</div>
                    <button
                      type="button"
                      onClick={() => {
                        setBannerDrafts((prev) => prev.map((item, idx) => idx === index ? {
                          ...item,
                          eventId: '',
                          title: '',
                          link: '',
                          imageUrl: '',
                        } : item))
                      }}
                      className="rounded-full border border-[var(--line)] bg-white px-3 py-1 text-[11px] font-semibold text-slate-500"
                    >
                      비우기
                    </button>
                  </div>
                  <div className="grid gap-3 lg:grid-cols-[1fr_1fr_1.2fr_1.6fr]">
                    <input
                      value={banner.eventId ?? ''}
                      onChange={(e) => {
                        const next = e.target.value.trim()
                        setBannerDrafts((prev) => prev.map((item, idx) => {
                          if (idx !== index) return item
                          if (!next) {
                            return { ...item, eventId: '', link: '', imageUrl: '' }
                          }
                          const eventLink = `/events/${next}`
                          return {
                            ...item,
                            eventId: next,
                            link: eventLink,
                            imageUrl: suggestBannerImageUrl(eventLink, allEvents as Event[]) || item.imageUrl,
                          }
                        }))
                      }}
                      placeholder="이벤트 UUID"
                      className="w-full rounded-full border border-[var(--line)] bg-white px-4 py-3 text-sm outline-none"
                    />
                    <input
                      value={banner.title}
                      onChange={(e) => {
                        const next = e.target.value
                        setBannerDrafts((prev) => prev.map((item, idx) => idx === index ? { ...item, title: next } : item))
                      }}
                      placeholder="배너 제목"
                      className="w-full rounded-full border border-[var(--line)] bg-white px-4 py-3 text-sm outline-none"
                    />
                    <input
                      value={banner.link}
                      onChange={(e) => {
                        const next = e.target.value
                        setBannerDrafts((prev) => prev.map((item, idx) => {
                          if (idx !== index) return item
                          const resolvedEventId = extractEventIdFromBannerLink(next)
                          const suggestedImageUrl = item.imageUrl.trim() ? item.imageUrl : suggestBannerImageUrl(next, allEvents as Event[])
                          return {
                            ...item,
                            link: next,
                            eventId: resolvedEventId || item.eventId,
                            imageUrl: suggestedImageUrl || item.imageUrl,
                          }
                        }))
                      }}
                      placeholder="링크 주소"
                      className="w-full rounded-full border border-[var(--line)] bg-white px-4 py-3 text-sm outline-none"
                    />
                    <input
                      value={banner.imageUrl}
                      onChange={(e) => {
                        const next = e.target.value
                        setBannerDrafts((prev) => prev.map((item, idx) => idx === index ? { ...item, imageUrl: next } : item))
                      }}
                      placeholder="이미지 주소"
                      className="w-full rounded-full border border-[var(--line)] bg-white px-4 py-3 text-sm outline-none"
                    />
                  </div>
                </div>
              ))}
            </div>

            <div className="mt-5 flex items-center justify-end gap-2">
              <button
                type="button"
                onClick={() => setIsBannerEditorOpen(false)}
                className="rounded-full border border-[var(--line)] bg-white px-4 py-2.5 text-sm font-semibold text-slate-700"
              >
                취소
              </button>
              <button
                type="button"
                onClick={() => {
                  const nextBanners = bannerDrafts
                    .map((banner, index) => ({
                      id: banner.id || `banner-${index}`,
                      title: String(banner.title ?? '').trim(),
                      link: String(banner.link ?? '').trim(),
                      imageUrl:
                        String(banner.imageUrl ?? '').trim() ||
                        suggestBannerImageUrl(String(banner.eventId ?? '').trim() ? `/events/${String(banner.eventId ?? '').trim()}` : String(banner.link ?? '').trim(), allEvents as Event[]),
                      eventId: String(banner.eventId ?? '').trim(),
                    }))
                    .filter((banner) => banner.title || banner.link || banner.imageUrl)
                  setHomeBanners((nextBanners.length ? nextBanners : DEFAULT_HOME_BANNERS).slice(0, 3))
                  setIsBannerEditorOpen(false)
                }}
                className="rounded-full bg-[var(--accent)] px-5 py-2.5 text-sm font-semibold text-white"
              >
                저장
              </button>
            </div>
          </div>
        </div>
      )}
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

function BannerCard({ banner }: { banner: HomeBanner }) {
  const internalPath = resolveInternalBannerPath(banner.link)
  const isInternalLink = Boolean(internalPath)
  const content = (
    <div className="relative h-full min-h-[88px] w-full overflow-hidden rounded-[20px] border border-[var(--line)] bg-white text-slate-950 shadow-[0_12px_30px_rgba(15,23,42,0.08)] md:rounded-[24px] md:bg-white md:text-slate-950">
      <div className="relative aspect-[16/9] w-full overflow-hidden rounded-[20px] bg-[#ece8ff] md:rounded-[24px]">
        {banner.imageUrl ? (
          <>
            <img
              src={banner.imageUrl}
              alt={banner.title}
              className="absolute inset-0 h-full w-full object-cover object-center blur-[14px] scale-110 opacity-35 saturate-125"
            />
            <img
              src={banner.imageUrl}
              alt={banner.title}
              className="absolute inset-5 h-[calc(100%-2.5rem)] w-[calc(100%-2.5rem)] rounded-[18px] object-cover object-center md:inset-6 md:h-[calc(100%-3rem)] md:w-[calc(100%-3rem)] md:rounded-[20px] opacity-95 shadow-[0_0_0_1px_rgba(255,255,255,0.4)]"
            />
          </>
        ) : (
          <>
            <div className="absolute inset-0 bg-gradient-to-br from-[#dcd2ff] via-[#cbbcff] to-[#efe2ff] opacity-95" />
            <div className="absolute inset-5 rounded-[18px] border border-white/75 bg-white/25 md:inset-6 md:rounded-[20px]" />
            <div className="absolute inset-0 flex items-center justify-center text-[16px] font-semibold text-slate-500/80">
              행사 이미지
            </div>
          </>
        )}
        <div className="pointer-events-none absolute inset-0 bg-[linear-gradient(180deg,rgba(255,255,255,0.04)_0%,rgba(194,163,255,0.07)_48%,rgba(15,23,42,0.10)_100%)]" />
        <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_50%_20%,rgba(196,164,255,0.12),transparent_34%)]" />
        <div className="absolute inset-x-4 bottom-4 md:inset-x-6 md:bottom-6">
          <div className="max-w-[78%]">
            <div className="text-[14px] font-black leading-5 tracking-tight text-white drop-shadow-[0_2px_10px_rgba(15,23,42,0.55)] md:text-[17px] md:leading-6">
              {banner.title || '배너 제목'}
            </div>
            <div className="mt-2 inline-flex w-fit items-center rounded-full bg-white/90 px-2.5 py-1 text-[10px] font-semibold text-[var(--accent)] shadow-[0_8px_22px_rgba(15,23,42,0.12)] backdrop-blur md:mt-3 md:px-3 md:py-1.5 md:text-[11px]">
              바로가기
            </div>
          </div>
        </div>
      </div>
    </div>
  )

  if (isInternalLink) {
    return (
      <Link to={internalPath} className="block w-full">
        {content}
      </Link>
    )
  }

  return (
    <a href={banner.link} target="_blank" rel="noreferrer" className="block w-full">
      {content}
    </a>
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

function suggestBannerImageUrl(link: string, events: Event[]) {
  const normalized = resolveInternalBannerPath(link) || String(link ?? '').trim()
  if (!normalized) return ''

  if (normalized.startsWith('/events/')) {
    const eventId = normalized.match(/^\/events\/([^/?#]+)/)?.[1]
    if (eventId) {
      const matchedEvent = events.find((event) => String(event.id) === eventId)
      if (matchedEvent?.img) return matchedEvent.img
    }
    return '/banner-event.svg'
  }

  if (normalized.startsWith('/events')) return '/banner-event.svg'
  if (normalized.startsWith('/community') || normalized.startsWith('/notice')) return '/banner-notice.svg'
  if (normalized.startsWith('/calendar')) return '/banner-event.svg'

  return ''
}

function resolveInternalBannerPath(link: string) {
  const normalized = String(link ?? '').trim()
  if (!normalized) return ''
  if (normalized.startsWith('/')) return normalized

  try {
    const url = new URL(normalized)
    if (typeof window !== 'undefined' && url.origin === window.location.origin) {
      return `${url.pathname}${url.search}${url.hash}`
    }
  } catch {
    // ignore invalid URLs
  }

  return ''
}

function extractEventIdFromBannerLink(link: string) {
  const normalized = resolveInternalBannerPath(link)
  if (!normalized) return ''
  return normalized.match(/^\/events\/([^/?#]+)/)?.[1] ?? ''
}
