import { useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getEvents } from '../api/events'
import type { Event } from '../types'
import { resolveRegionFromCoordinates } from '../lib/kakao'
import { useAuthStore } from '../store/authStore'

const REGION_FILTERS = ['전체', '서울', '경기', '충청', '강원', '경상', '전라', '부산', '제주']
const CATEGORY_FILTERS = ['전체', '콘서트', '축제', '팬미팅', '팝업스토어']
const STATUS_FILTERS = ['전체', '예정', '진행중', '종료']
const EVENT_CREATE_LINK = '/admin?panel=general'

export default function Events() {
  const { user } = useAuthStore()
  const [searchParams, setSearchParams] = useSearchParams()
  const [selectedRegion, setSelectedRegion] = useState(searchParams.get('region') ?? '전체')
  const [selectedCategory, setSelectedCategory] = useState(searchParams.get('category') ?? '전체')
  const [selectedStatus, setSelectedStatus] = useState(searchParams.get('status') ?? '전체')
  const [query, setQuery] = useState(searchParams.get('query') ?? '')
  const [resolvedRegions, setResolvedRegions] = useState<Record<string, string>>({})
  const regionCacheRef = useRef<Record<string, string>>({})
  const kakaoKey = String(import.meta.env.VITE_KAKAO_JS_KEY ?? '')
  const isManager = !!user && /ADMIN|MANAGER/.test(user.role)

  const { data: events = [] } = useQuery({
    queryKey: ['events', 'list'],
    queryFn: () => getEvents({ size: 100 }),
  })

  useEffect(() => {
    if (!kakaoKey) return
    let cancelled = false
    const pending = (events as Event[]).filter((event) => {
      if (event.region) return false
      if (regionCacheRef.current[event.id]) return false
      return event.latitude != null && event.longitude != null
    })

    if (pending.length === 0) return

    void Promise.all(
      pending.map(async (event) => {
        try {
          const resolved = await resolveRegionFromCoordinates(event.latitude!, event.longitude!)
          return resolved ? [event.id, resolved] as const : null
        } catch {
          return null
        }
      }),
    ).then((pairs) => {
      if (cancelled) return
      const next = { ...regionCacheRef.current }
      for (const pair of pairs) {
        if (!pair) continue
        next[pair[0]] = pair[1]
      }
      regionCacheRef.current = next
      setResolvedRegions(next)
    })

    return () => {
      cancelled = true
    }
  }, [events, kakaoKey])

  const filteredEvents = useMemo(() => {
    return sortUpcomingEvents(
      (events as Event[]).filter((event) => {
        const queryOk = !query.trim() || matchesSearchText([event.name, event.place, event.performer ?? '', event.categoryName].join(' '), query)
        const regionOk = selectedRegion === '전체' || getRegionLabel(event, resolvedRegions) === selectedRegion
        const categoryOk = selectedCategory === '전체'
          || normalizeCategoryKey(event.categoryName) === normalizeCategoryKey(selectedCategory)
        const statusOk = selectedStatus === '전체' || getDisplayStatus(event).label === selectedStatus
        return queryOk && regionOk && categoryOk && statusOk
      })
    )
  }, [events, query, resolvedRegions, selectedCategory, selectedRegion, selectedStatus])

  const submitSearch = () => {
    const next = query.trim()
    setSearchParams({
      ...(next ? { query: next } : {}),
      ...(selectedRegion !== '전체' ? { region: selectedRegion } : {}),
      ...(selectedCategory !== '전체' ? { category: selectedCategory } : {}),
      ...(selectedStatus !== '전체' ? { status: selectedStatus } : {}),
    })
  }

  return (
    <div className="space-y-6 px-5 py-5 md:px-8 md:py-7">
      <section className="rounded-[24px] border border-[var(--line)] bg-white p-4 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-6">
        <div className="grid gap-5 xl:grid-cols-[1.05fr_0.95fr] xl:items-end">
          <div className="space-y-3">
            <div className="inline-flex rounded-full bg-[var(--accent-soft)] px-3 py-1 text-xs font-semibold text-[var(--accent)]">
              행사
            </div>
            <h1 className="text-[20px] font-black tracking-tight text-slate-950 md:text-[30px]">
              검색하고, 필터하고, 카드로 빠르게 훑어보기
            </h1>
            <p className="hidden max-w-2xl text-sm leading-6 text-slate-600 md:block">
              홈에서 검색한 내용은 여기서 자세히 볼 수 있고, 지역과 카테고리 필터도 이 화면에서 바로 조합할 수 있어요.
            </p>
          </div>

          <div className="w-full xl:ml-auto xl:max-w-[420px] xl:justify-self-end">
            <label className="flex items-center gap-2 rounded-full border border-[var(--line)] bg-white px-3 py-2.5">
              <span className="text-sm text-slate-400">⌕</span>
              <input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault()
                    submitSearch()
                  }
                }}
                placeholder="행사명, 장소, 아티스트"
                className="min-w-0 flex-1 bg-transparent text-sm outline-none"
              />
              <button
                onClick={submitSearch}
                aria-label="검색"
                className="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-[var(--accent-soft)] text-[var(--accent)] transition-colors hover:bg-white"
              >
                ⌕
              </button>
            </label>
          </div>
        </div>

        <div className="mt-4 space-y-3">
          <div className="grid gap-3 md:hidden">
            <div className="flex items-center gap-2">
              <span className="text-[11px] font-semibold text-slate-500">지역</span>
              <select
                value={selectedRegion}
                onChange={(e) => setSelectedRegion(e.target.value)}
                className="min-w-[120px] rounded-full border border-[var(--line)] bg-white px-3 py-2 text-[11px] font-semibold text-slate-700 outline-none"
              >
                {REGION_FILTERS.map((item) => (
                  <option key={item} value={item}>
                    {item}
                  </option>
                ))}
              </select>
            </div>
            <FilterRow label="카테고리">
              {CATEGORY_FILTERS.map((item) => (
                <Chip
                  key={item}
                  active={selectedCategory === item}
                  tone={item === '전체' ? 'slate' : getCategoryTone(item)}
                  onClick={() => setSelectedCategory(item)}
                >
                  {item}
                </Chip>
              ))}
            </FilterRow>
            <FilterRow label="상태">
              {STATUS_FILTERS.map((item) => (
                <Chip
                  key={item}
                  active={selectedStatus === item}
                  tone={item === '전체' ? 'slate' : item === '예정' ? 'violet' : item === '진행중' ? 'emerald' : 'slate'}
                  onClick={() => setSelectedStatus(item)}
                >
                  {item}
                </Chip>
              ))}
            </FilterRow>
          </div>

          <div className="hidden mt-6 flex-wrap items-center gap-3 md:flex">
            <FilterRow label="지역">
              {REGION_FILTERS.map((item) => (
                <Chip key={item} active={selectedRegion === item} onClick={() => setSelectedRegion(item)}>
                  {item}
                </Chip>
              ))}
            </FilterRow>
            <FilterRow label="카테고리">
              {CATEGORY_FILTERS.map((item) => (
                <Chip
                  key={item}
                  active={selectedCategory === item}
                  tone={item === '전체' ? 'slate' : getCategoryTone(item)}
                  onClick={() => setSelectedCategory(item)}
                >
                  {item}
                </Chip>
              ))}
            </FilterRow>
            <FilterRow label="상태">
              {STATUS_FILTERS.map((item) => (
                <Chip
                  key={item}
                  active={selectedStatus === item}
                  tone={item === '전체' ? 'slate' : item === '예정' ? 'violet' : item === '진행중' ? 'emerald' : 'slate'}
                  onClick={() => setSelectedStatus(item)}
                >
                  {item}
                </Chip>
              ))}
            </FilterRow>
          </div>
        </div>
      </section>

      <section className="space-y-4 rounded-[24px] border border-[var(--line)] bg-white p-4 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-5">
        <div className="flex items-center justify-between gap-3">
          <div>
            <h2 className="text-[18px] font-black tracking-tight text-slate-950">행사 결과</h2>
            <p className="mt-1 text-xs text-slate-500">{filteredEvents.length}개의 행사를 보고 있어요.</p>
          </div>
          {isManager && (
            <Link
              to={EVENT_CREATE_LINK}
              className="rounded-full bg-[var(--accent-soft)] px-4 py-2 text-xs font-semibold text-[var(--accent)] transition-colors hover:bg-white"
            >
              행사생성
            </Link>
          )}
        </div>

        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {filteredEvents.map((event) => (
            <EventNewsCard key={event.id} event={event} />
          ))}
        </div>
      </section>
    </div>
  )
}

function EventNewsCard({ event }: { event: Event }) {
  const displayStatus = getDisplayStatus(event)
  const categoryTone = getCategoryTone(event.categoryName)
  return (
    <Link
      to={`/events/${event.id}`}
      className={`flex h-full overflow-hidden rounded-[26px] border shadow-[0_12px_24px_rgba(15,23,42,0.04)] transition-transform hover:-translate-y-0.5 md:flex md:flex-col ${
        displayStatus.isPast
          ? 'border-[rgba(226,217,245,0.85)] bg-[linear-gradient(180deg,rgba(250,248,255,0.95),rgba(243,238,255,0.88))] opacity-90'
          : 'border-[rgba(226,217,245,0.95)] bg-[linear-gradient(180deg,rgba(255,255,255,0.98),rgba(250,248,255,0.94))]'
      }`}
    >
      {event.img ? (
        <div className="relative flex h-auto w-[104px] shrink-0 overflow-hidden rounded-l-[26px] md:h-36 md:w-full md:rounded-none md:p-3">
          <div className="absolute inset-0 bg-[linear-gradient(135deg,rgba(238,232,255,0.82),rgba(245,243,255,0.58),rgba(249,168,212,0.20))]" />
          <div className="relative flex h-full w-full items-center justify-center p-2 md:p-0">
            <img
              src={event.img}
              alt={event.name}
              className="h-full w-full rounded-[18px] object-cover shadow-[0_0_0_1px_rgba(148,163,184,0.18)] md:rounded-[18px]"
            />
          </div>
        </div>
      ) : (
        <div
          style={cardTone(event.categoryName)}
          className="flex h-auto w-[104px] shrink-0 items-center justify-center rounded-l-[26px] px-3 py-3 md:h-36 md:w-full md:rounded-none md:px-4 md:py-3"
        >
          <div className="flex h-24 w-full items-center justify-center rounded-[18px] border border-white/60 bg-white/30 text-[11px] font-semibold text-white/90 md:h-full md:text-sm">
            행사 이미지
          </div>
        </div>
      )}
      <div className="min-w-0 flex h-full flex-1 flex-col space-y-2.5 p-3 md:space-y-3 md:p-4">
        <div className="flex flex-wrap items-center gap-2">
          <Badge tone={categoryTone}>{displayEventCategoryLabel(event.categoryName)}</Badge>
          <span className={`text-xs font-semibold ${displayStatus.statusClass}`}>{displayStatus.label}</span>
        </div>
        <div>
          <h3 className="line-clamp-2 text-[15px] font-black tracking-tight text-slate-950 md:text-[16px]">{event.name}</h3>
          <p className="mt-1 line-clamp-1 text-xs leading-5 text-slate-600 md:mt-2 md:line-clamp-2 md:text-sm md:leading-6">{event.description ?? '행사 정보를 확인해보세요.'}</p>
        </div>
        <div className="mt-auto space-y-1 border-t border-slate-100 pt-2 md:space-y-1.5 md:pt-3">
          <MiniRow label="일정" value={formatShortRange(event.startAt, event.endAt)} />
          <MiniRow label="장소" value={event.place} />
          <MiniRow label="티켓팅" value={event.ticketingOpenAt ? formatDate(event.ticketingOpenAt) : '미정'} />
        </div>
      </div>
    </Link>
  )
}

function FilterRow({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="flex flex-wrap items-center gap-1.5">
      <span className="text-[11px] font-semibold text-slate-500">{label}</span>
      {children}
    </div>
  )
}

function Chip({
  active,
  tone = 'slate',
  onClick,
  children,
}: {
  active?: boolean
  tone?: 'violet' | 'sky' | 'emerald' | 'rose' | 'slate' | 'concert' | 'festival' | 'fanmeeting' | 'popup'
  onClick: () => void
  children: ReactNode
}) {
  const toneClass = {
    violet: 'border-[var(--accent)] bg-[var(--accent-soft)] text-[var(--accent)]',
    sky: 'border-sky-200 bg-sky-50 text-sky-700',
    emerald: 'border-emerald-200 bg-emerald-50 text-emerald-700',
    rose: 'border-rose-200 bg-rose-50 text-rose-700',
    slate: 'border-slate-200 bg-slate-50 text-slate-700',
    concert: 'border-violet-200 bg-violet-50 text-violet-700',
    festival: 'border-fuchsia-200 bg-fuchsia-50 text-fuchsia-700',
    fanmeeting: 'border-pink-200 bg-pink-50 text-pink-700',
    popup: 'border-sky-200 bg-sky-50 text-sky-700',
  }[tone]

  return (
    <button
      onClick={onClick}
      className={`rounded-full border px-2 py-1 text-[10px] font-semibold transition-colors md:px-2.5 md:py-1.5 md:text-[11px] ${
        active ? toneClass : 'border-[var(--line)] bg-white text-slate-600 hover:bg-slate-50'
      }`}
    >
      {children}
    </button>
  )
}

function Badge({ children, tone }: { children: ReactNode; tone?: 'concert' | 'festival' | 'fanmeeting' | 'popup' }) {
  const toneClass = {
    concert: 'bg-violet-100 text-violet-700',
    festival: 'bg-fuchsia-100 text-fuchsia-700',
    fanmeeting: 'bg-pink-100 text-pink-700',
    popup: 'bg-sky-100 text-sky-700',
  }[tone ?? 'concert']

  return <span className={`rounded-full px-2.5 py-1 text-[11px] font-semibold ${toneClass}`}>{children}</span>
}

function cardTone(categoryName: string) {
  return (
    {
      concert: { background: 'linear-gradient(135deg, rgba(167,139,250,0.18), rgba(124,58,237,0.42))' },
      festival: { background: 'linear-gradient(135deg, rgba(244,114,182,0.18), rgba(236,72,153,0.38))' },
      fanmeeting: { background: 'linear-gradient(135deg, rgba(251,113,133,0.18), rgba(244,63,94,0.34))' },
      popup: { background: 'linear-gradient(135deg, rgba(125,211,252,0.16), rgba(96,165,250,0.30))' },
    }[normalizeCategoryKey(categoryName)] ?? { background: 'linear-gradient(135deg, rgba(111,84,255,0.16), rgba(111,84,255,0.32))' }
  )
}

function sortUpcomingEvents(events: Event[]) {
  return [...events].sort((a, b) => {
    const aBucket = getSortBucket(a)
    const bBucket = getSortBucket(b)
    if (aBucket !== bBucket) return aBucket - bBucket

    if (aBucket === 0) return String(a.startAt ?? '').localeCompare(String(b.startAt ?? ''))
    if (aBucket === 1) return String(a.startAt ?? '').localeCompare(String(b.startAt ?? ''))
    return String(b.endAt ?? '').localeCompare(String(a.endAt ?? ''))
  })
}

function getSortBucket(event: Event) {
  if (isOngoingEvent(event)) return 0
  if (isFutureEvent(event)) return 1
  return 2
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

function getCategoryTone(name: string) {
  return (normalizeCategoryKey(name) === 'festival'
    ? 'festival'
    : normalizeCategoryKey(name) === 'fanmeeting'
      ? 'fanmeeting'
      : normalizeCategoryKey(name) === 'popup'
        ? 'popup'
        : 'concert') as 'concert' | 'festival' | 'fanmeeting' | 'popup'
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

function getDisplayStatus(event: Event) {
  if (isCancelled(event)) {
    return { label: '취소', statusClass: 'text-rose-500', isPast: true }
  }
  if (isOngoingEvent(event)) {
    return { label: '진행중', statusClass: 'text-emerald-600', isPast: false }
  }
  if (isFutureEvent(event)) {
    return { label: '예정', statusClass: 'text-[var(--accent)]', isPast: false }
  }
  return { label: '종료', statusClass: 'text-slate-400', isPast: true }
}

function isOngoingEvent(event: Event) {
  const now = new Date()
  return new Date(event.startAt) <= now && now <= new Date(event.endAt)
}

function isFutureEvent(event: Event) {
  return new Date(event.startAt) > new Date()
}

function isCancelled(event: Event) {
  return event.status === 'CANCELLED'
}

function matchesSearchText(source: string, query: string) {
  const normalizedSource = normalizeSearch(source)
  const normalizedQuery = normalizeSearch(query)
  if (!normalizedQuery) return true
  if (normalizedSource.includes(normalizedQuery)) return true
  return toInitialConsonants(source).includes(toInitialConsonants(query))
}

function normalizeSearch(value: string) {
  return value.normalize('NFKD').replace(/\s+/g, '').toLowerCase()
}

function toInitialConsonants(value: string) {
  const initials = 'ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ'
  let result = ''

  for (const char of value.normalize('NFC')) {
    const code = char.codePointAt(0) ?? 0
    if (code >= 0xac00 && code <= 0xd7a3) {
      result += initials[Math.floor((code - 0xac00) / 588)]
      continue
    }
    if (/\s/.test(char)) continue
    result += char.toLowerCase()
  }

  return result
}

function formatDate(value?: string | null) {
  if (!value) return '미정'
  return new Date(value).toLocaleDateString('ko-KR', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function formatShortRange(startAt?: string | null, endAt?: string | null) {
  if (!startAt) return '일정 미정'
  const start = new Date(startAt)
  const end = endAt ? new Date(endAt) : null
  const startText = start.toLocaleDateString('ko-KR', { month: '2-digit', day: '2-digit' })
  const endText = end ? end.toLocaleDateString('ko-KR', { month: '2-digit', day: '2-digit' }) : ''
  return end ? `${startText} - ${endText}` : startText
}

function getRegionLabel(event: Event, resolvedRegions: Record<string, string>) {
  const rawRegion = event.region ?? resolvedRegions[event.id] ?? ''
  const normalized = normalizeRegionLabel(rawRegion)
  if (normalized !== '경상') return normalized
  const placeHint = normalizeRegionLabel(event.place)
  return placeHint === '부산' ? '부산' : normalized
}

function normalizeRegionLabel(value?: string | null) {
  if (!value) return ''
  const compact = value.replace(/\s+/g, '')
  if (/서울/.test(compact)) return '서울'
  if (/경기/.test(compact)) return '경기'
  if (/세종|대전|충청/.test(compact)) return '충청'
  if (/강원/.test(compact)) return '강원'
  if (/부산/.test(compact)) return '부산'
  if (/경상|대구|울산|창원|포항|경주|진주|구미|경산/.test(compact)) return '경상'
  if (/전라|광주|전주|목포|여수|순천|군산|익산/.test(compact)) return '전라'
  if (/제주/.test(compact)) return '제주'
  return compact
}

function MiniRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between gap-3 text-xs">
      <span className="text-slate-500">{label}</span>
      <span className="truncate text-right font-semibold text-slate-900">{value}</span>
    </div>
  )
}
