import { useMemo, useState, type ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { deleteCalendar, getCalendars, updateCalendar } from '../api/calendar'
import { getEvents } from '../api/events'
import { useAuthStore } from '../store/authStore'
import { formatDateTime, getMonthKey } from '../lib/format'

export default function Calendar({ mode }: { mode: 'all' | 'mine' }) {
  const today = new Date()
  const queryClient = useQueryClient()
  const [year, setYear] = useState(today.getFullYear())
  const [month, setMonth] = useState(today.getMonth() + 1)
  const [selectedDay, setSelectedDay] = useState<number>(today.getDate())
  const [mineSort, setMineSort] = useState<'oldest' | 'latest'>('latest')
  const [mineCategory, setMineCategory] = useState('전체')
  const [memoDrafts, setMemoDrafts] = useState<Record<string, string>>({})
  const { isLoggedIn } = useAuthStore()

  const { data: events = [] } = useQuery({
    queryKey: ['events', 'calendar', year, month],
    queryFn: () => getEvents({ year, month, size: 100 }),
    enabled: mode === 'all',
  })

  const { data: allEvents = [] } = useQuery({
    queryKey: ['events', 'mine', 'lookup'],
    queryFn: () => getEvents({ size: 100 }),
    enabled: mode === 'mine' && isLoggedIn(),
  })

  const { data: calendars = [] } = useQuery({
    queryKey: ['calendars', year, month],
    queryFn: () => getCalendars(year, month),
    enabled: mode === 'mine' && isLoggedIn(),
  })

  const deleteMutation = useMutation({
    mutationFn: (calendarId: string) => deleteCalendar(calendarId),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['calendars', year, month] })
    },
  })

  const updateMutation = useMutation({
    mutationFn: ({ calendarId, memo }: { calendarId: string; memo: string }) => updateCalendar(calendarId, { memo }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['calendars', year, month] })
    },
  })

  const eventsByDay = useMemo(() => {
    const map = new Map<string, any[]>()
    events.forEach((event: any) => {
      const start = startOfDay(new Date(event.startAt))
      const end = startOfDay(new Date(event.endAt ?? event.startAt))
      for (let cursor = new Date(start); cursor <= end; cursor.setDate(cursor.getDate() + 1)) {
        const key = getMonthKey(cursor.getFullYear(), cursor.getMonth() + 1, cursor.getDate())
        const list = map.get(key) ?? []
        list.push(event)
        map.set(key, list)
      }
    })
    return map
  }, [events])

  const calendarByDay = useMemo(() => {
    const map = new Map<string, any[]>()
    calendars.forEach((item: any) => {
      const date = new Date(item.eventDate)
      const key = getMonthKey(date.getFullYear(), date.getMonth() + 1, date.getDate())
      const list = map.get(key) ?? []
      list.push(item)
      map.set(key, list)
    })
    return map
  }, [calendars])

  const eventById = useMemo(() => {
    const map = new Map<string, any>()
    allEvents.forEach((event: any) => map.set(event.id, event))
    return map
  }, [allEvents])

  const mineCards = useMemo(() => {
    return [...calendars]
      .map((item: any) => {
        const event = eventById.get(item.eventId)
        return {
          ...item,
          event,
          categoryName: event?.categoryName ?? '기타',
        }
      })
      .sort((a: any, b: any) => mineSort === 'oldest'
        ? String(a.eventDate ?? '').localeCompare(String(b.eventDate ?? ''))
        : String(b.eventDate ?? '').localeCompare(String(a.eventDate ?? '')))
  }, [calendars, eventById, mineSort])

  const mineCategories = useMemo(() => {
    const fallback = ['콘서트', '축제', '팬미팅', '팝업스토어']
    const discovered = Array.from(new Set([...mineCards.map((item: any) => item.categoryName), ...fallback]))
    const ordered = [...fallback.filter((item) => discovered.includes(item)), ...discovered.filter((item) => !fallback.includes(item))]
    return ['전체', ...ordered]
  }, [mineCards])

  const filteredMineCards = useMemo(() => {
    return mineCards.filter((item: any) => mineCategory === '전체' || item.categoryName === mineCategory)
  }, [mineCards, mineCategory])

  const visibleMineCards = useMemo(() => {
    return filteredMineCards.filter((item: any) => {
      const date = new Date(item.eventDate)
      return date.getFullYear() === year && date.getMonth() + 1 === month
    })
  }, [filteredMineCards, month, year])

  const currentMonthEvents = useMemo(() => {
    const seen = new Set<string>()
    return [...events].filter((event: any) => {
      if (seen.has(event.id)) return false
      const start = new Date(event.startAt)
      const end = new Date(event.endAt ?? event.startAt)
      const monthStart = new Date(year, month - 1, 1)
      const monthEnd = new Date(year, month, 0, 23, 59, 59, 999)
      const overlapsMonth = start <= monthEnd && end >= monthStart
      if (!overlapsMonth) return false
      seen.add(event.id)
      return true
    })
  }, [events, month, year])

  const firstDay = new Date(year, month - 1, 1).getDay()
  const daysInMonth = new Date(year, month, 0).getDate()
  const title = mode === 'all' ? '전체 행사로 만드는 공개 캘린더' : '내가 추가한 일정만 따로 보는 캘린더'
  const description =
    mode === 'all'
      ? '전체 캘린더는 행사 데이터만으로 만들고, 내 일정은 백엔드에 저장된 카드와 메모만 보여줍니다.'
      : '내 일정은 데이터베이스에 저장된 개인 일정입니다. 캘린더와 연결된 개념이 아니라 카드와 메모를 따로 읽는 화면이에요.'

  return (
    <div className="space-y-6 px-5 py-5 md:px-8 md:py-7">
      <section className="rounded-[24px] border border-[var(--line)] bg-white p-4 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-6">
        <div className="inline-flex rounded-full bg-[var(--accent-soft)] px-3 py-1 text-xs font-semibold text-[var(--accent)]">
          {mode === 'all' ? '캘린더' : '내 일정'}
        </div>
        <h1 className="mt-2 text-[22px] font-black tracking-tight text-slate-950 md:mt-3 md:text-[28px]">{title}</h1>
        <p className="mt-1 hidden max-w-xl text-sm leading-6 text-slate-600 md:mt-2 md:block">{description}</p>
      </section>

      {mode === 'all' ? (
        <div className="grid gap-6 xl:grid-cols-[minmax(0,3fr)_minmax(0,1fr)]">
          <section className="min-w-0 rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
            <div className="mb-3 flex items-center justify-between">
              <div className="text-lg font-black tracking-tight text-slate-950">{year}년 {month}월</div>
              <div className="flex items-center gap-2">
                <button onClick={() => shiftMonth(-1, year, month, setYear, setMonth, setSelectedDay)} className="rounded-full border border-[var(--line)] bg-white px-3 py-2 text-sm font-medium text-slate-700 sm:px-4">
                  <span className="sm:hidden">‹</span>
                  <span className="hidden sm:inline">이전</span>
                </button>
                <button onClick={() => shiftMonth(1, year, month, setYear, setMonth, setSelectedDay)} className="rounded-full border border-[var(--line)] bg-white px-3 py-2 text-sm font-medium text-slate-700 sm:px-4">
                  <span className="sm:hidden">›</span>
                  <span className="hidden sm:inline">다음</span>
                </button>
              </div>
            </div>

            <div className="grid grid-cols-7 gap-px overflow-hidden rounded-[20px] border border-slate-200 bg-slate-200">
              {['일', '월', '화', '수', '목', '금', '토'].map((day) => (
                <div key={day} className="bg-slate-50 py-2 text-center text-[11px] font-semibold text-slate-600">{day}</div>
              ))}
              {Array.from({ length: firstDay }).map((_, idx) => <div key={`blank-${idx}`} className="min-h-16 bg-slate-50 sm:min-h-24" />)}
              {Array.from({ length: daysInMonth }).map((_, idx) => {
                const day = idx + 1
                const key = getMonthKey(year, month, day)
                const publicEvents = eventsByDay.get(key) ?? []
                const isToday = today.getFullYear() === year && today.getMonth() + 1 === month && today.getDate() === day
                const isSelected = selectedDay === day

                return (
                  <button
                    key={day}
                    onClick={() => setSelectedDay(day)}
                    className={`min-h-16 bg-white p-1.5 text-left transition-colors hover:bg-[var(--accent-soft)]/40 sm:min-h-24 sm:p-2.5 ${
                      isSelected ? 'bg-[var(--accent-soft)]' : ''
                    }`}
                  >
                    <div className={`inline-flex h-6 w-6 items-center justify-center rounded-full text-[11px] font-semibold sm:h-7 sm:w-7 sm:text-xs ${
                      isToday ? 'bg-[var(--accent)] text-white' : isSelected ? 'bg-[var(--accent-soft)] text-[var(--accent)]' : 'text-slate-900'
                    }`}>
                      {day}
                    </div>
                    <div className="mt-1 space-y-1">
                      {publicEvents.slice(0, 2).map((entry: any, index: number) => (
                        <div
                          key={`${entry.id ?? index}`}
                          className="hidden truncate rounded-md border border-[rgba(111,84,255,0.12)] bg-[linear-gradient(90deg,rgba(111,84,255,0.20),rgba(111,84,255,0.08))] px-2 py-1 text-[11px] font-medium text-slate-700 sm:block"
                        >
                          {entry.eventName ?? entry.name}
                        </div>
                      ))}
                      {publicEvents.length > 0 && (
                        <div className="flex flex-wrap gap-1 sm:hidden">
                          {publicEvents.slice(0, 3).map((entry: any, index: number) => (
                            <span key={`${entry.id ?? index}`} className="h-1.5 w-1.5 rounded-full bg-[var(--accent)]" />
                          ))}
                        </div>
                      )}
                    </div>
                  </button>
                )
              })}
              {Array.from({ length: (7 - ((firstDay + daysInMonth) % 7)) % 7 }).map((_, idx) => (
                <div key={`tail-blank-${idx}`} className="min-h-16 bg-slate-50 sm:min-h-24" />
              ))}
            </div>

            <div className="mt-4 rounded-[20px] border border-[var(--line)] bg-slate-50 p-4">
              <div className="flex items-center justify-between">
                <div className="text-sm font-semibold text-slate-500">선택한 날짜</div>
                <div className="text-xs text-slate-400">{year}.{String(month).padStart(2, '0')}.{String(selectedDay).padStart(2, '0')}</div>
              </div>
              <div className="mt-3 space-y-2">
                {(eventsByDay.get(getMonthKey(year, month, selectedDay)) ?? []).slice(0, 3).map((event: any) => (
                  <Link
                    key={event.id}
                    to={`/events/${event.id}`}
                    className="block rounded-[16px] border border-[rgba(111,84,255,0.12)] bg-[linear-gradient(90deg,rgba(111,84,255,0.14),rgba(255,255,255,0.92))] px-3 py-2 text-sm text-slate-700 shadow-sm"
                  >
                    <div className="font-semibold text-slate-950">{event.name}</div>
                    <div className="mt-1 text-xs text-slate-500">{event.place}</div>
                  </Link>
                ))}
                {(eventsByDay.get(getMonthKey(year, month, selectedDay)) ?? []).length === 0 && (
                  <div className="text-sm text-slate-500">이 날짜에 공개 행사 정보가 없어요.</div>
                )}
              </div>
            </div>
          </section>

          <aside className="hidden min-w-0 space-y-4 xl:block">
            <section className="rounded-[24px] border border-[var(--line)] bg-[#faf8ff] p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
              <div className="flex items-center justify-between">
                <h2 className="text-[18px] font-black tracking-tight text-slate-950">이번 달 행사 목록</h2>
                <span className="text-xs text-slate-500">{currentMonthEvents.length}개</span>
              </div>
              <div className="mt-4 space-y-2">
                {currentMonthEvents.slice(0, 4).map((event: any) => (
                  <Link key={event.id} to={`/events/${event.id}`} className="block rounded-[18px] border border-[var(--line)] bg-white p-3 transition-colors hover:bg-[var(--accent-soft)]">
                    <div className="flex items-start justify-between gap-3">
                      <div className="min-w-0 flex-1">
                        <div className="text-xs font-semibold text-[var(--accent)]">{event.categoryName}</div>
                        <div className="mt-1 truncate text-sm font-semibold text-slate-950">{event.name}</div>
                        <div className="mt-1 truncate text-xs text-slate-500">{event.place}</div>
                      </div>
                      <div className="shrink-0 text-right text-[11px] text-slate-400">{formatDateTime(event.startAt)}</div>
                    </div>
                  </Link>
                ))}
              </div>
            </section>

            <section className="rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
              <div className="flex items-center justify-between">
                <h2 className="text-[18px] font-black tracking-tight text-slate-950">안내</h2>
                <span className="text-xs text-slate-500">전체 캘린더</span>
              </div>
              <p className="mt-2 text-sm leading-6 text-slate-600">
                이 화면은 행사 정보를 기반으로 만든 공개 캘린더입니다. 내가 추가한 일정은 내 일정 탭에서 별도로 관리합니다.
              </p>
            </section>
          </aside>
        </div>
      ) : (
        <div className="grid gap-6 xl:grid-cols-[minmax(0,3fr)_minmax(0,2fr)]">
          <section className="min-w-0 space-y-3 rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
            <div className="flex items-center justify-between gap-3">
              <div>
                <h2 className="text-[18px] font-black tracking-tight text-slate-950">내 일정 카드</h2>
                <div className="mt-1 text-xs text-slate-500">{year}년 {month}월 · {visibleMineCards.length}개</div>
              </div>
              <div className="flex items-center gap-2">
                <SortPill active={mineSort === 'latest'} onClick={() => setMineSort('latest')}>최신순</SortPill>
                <SortPill active={mineSort === 'oldest'} onClick={() => setMineSort('oldest')}>오래된순</SortPill>
              </div>
            </div>

            <div className="flex flex-wrap items-center gap-1.5">
              <span className="text-[10px] font-semibold text-slate-500">행사 카테고리</span>
              {mineCategories.map((category) => (
                <SortPill key={category} active={mineCategory === category} onClick={() => setMineCategory(category)}>
                  {category}
                </SortPill>
              ))}
            </div>

            {isLoggedIn() ? (
              <div className="grid gap-3 md:grid-cols-2">
                {visibleMineCards.length === 0 ? (
                  <div className="rounded-[18px] border border-[var(--line)] bg-slate-50 p-4 text-sm text-slate-500 md:col-span-2">
                    추가한 일정이 아직 없어요.
                  </div>
                ) : visibleMineCards.map((item: any) => {
                  const statusLabel = getMineStatusLabel(item)
                  const isEnded = statusLabel === '종료' || statusLabel === '취소'
                  const memoValue = memoDrafts[item.id] ?? item.memo ?? ''

                  return (
                    <div
                      key={item.id}
                      className={`rounded-[18px] border p-2.5 ${isEnded ? 'border-slate-200 bg-slate-50 opacity-85' : 'border-[var(--line)] bg-white'}`}
                    >
                      <Link to={item.eventId ? `/events/${item.eventId}` : '#'} className="flex gap-2.5 rounded-[16px] p-1 transition-colors hover:bg-slate-50">
                        <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-[14px] bg-[var(--accent-soft)] text-center text-[10px] font-semibold text-[var(--accent)]">
                          {item.eventName.slice(0, 4) || '일정'}
                        </div>
                        <div className="min-w-0 flex-1">
                          <div className={`text-[10px] font-semibold ${getMineStatusStyle(item)}`}>{statusLabel}</div>
                          <div className="mt-0.5 truncate text-sm font-semibold text-slate-950">{item.eventName}</div>
                          <div className="mt-0.5 text-[11px] text-slate-500">{formatDateTime(item.eventDate)}</div>
                        </div>
                      </Link>

                      <div className="mt-2">
                        <div className="text-[10px] font-semibold text-slate-500">메모</div>
                        <input
                          value={memoValue}
                          onChange={(e) => setMemoDrafts((prev) => ({ ...prev, [item.id]: e.target.value }))}
                          placeholder="메모를 입력하세요"
                          className="mt-1 w-full rounded-2xl border border-slate-200 bg-white px-3 py-2.5 text-xs text-slate-700 outline-none ring-1 ring-transparent transition placeholder:text-slate-300 focus:border-[var(--accent)] focus:ring-[var(--accent-soft)]/60"
                        />
                      </div>

                      <div className="mt-2 flex items-center justify-between gap-2">
                      <button
                        type="button"
                        onClick={() => deleteMutation.mutate(item.id)}
                        disabled={deleteMutation.isPending}
                        className="inline-flex h-7 w-7 items-center justify-center rounded-full border border-rose-200 bg-rose-50 text-[12px] text-rose-600 disabled:opacity-70"
                        aria-label="일정 삭제"
                        title="일정 삭제"
                      >
                        🗑
                      </button>
                        <button
                          type="button"
                          onClick={() => updateMutation.mutate({ calendarId: item.id, memo: memoValue })}
                          disabled={updateMutation.isPending}
                          className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-[11px] font-semibold text-slate-700 disabled:opacity-70"
                        >
                          저장
                        </button>
                      </div>
                    </div>
                  )
                })}
              </div>
            ) : (
              <div className="rounded-[18px] border border-[var(--line)] bg-slate-50 p-4 text-sm text-slate-500">
                로그인하면 내가 추가한 일정과 메모를 카드로 확인할 수 있어요.
              </div>
            )}
          </section>

          <aside className="hidden min-w-0 space-y-4 xl:block">
            <section className="rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
              <div className="flex items-center justify-between">
                <h2 className="text-[18px] font-black tracking-tight text-slate-950">내 일정 달력</h2>
                <div className="flex items-center gap-2">
                  <button onClick={() => shiftMonth(-1, year, month, setYear, setMonth, setSelectedDay)} className="rounded-full border border-[var(--line)] bg-white px-3 py-2 text-sm font-medium text-slate-700 sm:px-4">
                    <span className="sm:hidden">‹</span>
                    <span className="hidden sm:inline">이전</span>
                  </button>
                  <button onClick={() => shiftMonth(1, year, month, setYear, setMonth, setSelectedDay)} className="rounded-full border border-[var(--line)] bg-white px-3 py-2 text-sm font-medium text-slate-700 sm:px-4">
                    <span className="sm:hidden">›</span>
                    <span className="hidden sm:inline">다음</span>
                  </button>
                </div>
              </div>
              <div className="mt-2 text-sm font-semibold text-slate-500">
                {year}년 {month}월
              </div>
              <div className="mt-4 grid grid-cols-7 gap-px overflow-hidden rounded-[20px] border border-slate-200 bg-slate-200">
                {['일', '월', '화', '수', '목', '금', '토'].map((day) => (
                  <div key={day} className="bg-slate-50 py-2 text-center text-[11px] font-semibold text-slate-600">{day}</div>
                ))}
                {Array.from({ length: firstDay }).map((_, idx) => <div key={`mine-blank-${idx}`} className="min-h-14 bg-slate-50" />)}
                {Array.from({ length: daysInMonth }).map((_, idx) => {
                  const day = idx + 1
                  const key = getMonthKey(year, month, day)
                  const saved = calendarByDay.get(key) ?? []
                  const isToday = today.getFullYear() === year && today.getMonth() + 1 === month && today.getDate() === day
                  const isSelected = selectedDay === day

                  return (
                    <button
                      key={day}
                      type="button"
                      onClick={() => setSelectedDay(day)}
                      className={`min-h-14 bg-white p-1 text-left transition-colors hover:bg-[var(--accent-soft)]/40 ${
                        isSelected ? 'bg-[var(--accent-soft)]' : ''
                      }`}
                    >
                      <div className={`inline-flex h-6 w-6 items-center justify-center rounded-full text-[11px] font-semibold ${
                        isToday ? 'bg-[var(--accent)] text-white' : isSelected ? 'bg-[var(--accent-soft)] text-[var(--accent)]' : 'text-slate-900'
                      }`}>
                        {day}
                      </div>
                      {saved.slice(0, 1).map((entry: any, index: number) => (
                        <div key={`${entry.id ?? index}`} className="mt-1 truncate rounded-md bg-[var(--accent-soft)] px-1.5 py-0.5 text-[10px] text-[var(--accent)]">
                          {entry.eventName}
                        </div>
                      ))}
                    </button>
                  )
                })}
                {Array.from({ length: (7 - ((firstDay + daysInMonth) % 7)) % 7 }).map((_, idx) => (
                  <div key={`mine-tail-blank-${idx}`} className="min-h-14 bg-slate-50" />
                ))}
              </div>
            </section>
          </aside>
        </div>
      )}
    </div>
  )
}

function shiftMonth(step: number, year: number, month: number, setYear: (v: number) => void, setMonth: (v: number) => void, setSelectedDay?: (v: number) => void) {
  const next = new Date(year, month - 1 + step, 1)
  setYear(next.getFullYear())
  setMonth(next.getMonth() + 1)
  if (setSelectedDay) setSelectedDay(1)
}

function SortPill({ active, onClick, children }: { active?: boolean; onClick: () => void; children: ReactNode }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`rounded-full border px-1.5 py-0.5 text-[9px] font-semibold transition-colors md:px-2.5 md:py-1 md:text-[10px] ${
        active
          ? 'border-[var(--accent)] bg-[var(--accent-soft)] text-[var(--accent)]'
          : 'border-[var(--line)] bg-white text-slate-600 hover:bg-slate-50'
      }`}
    >
      {children}
    </button>
  )
}

function getMineStatusLabel(item: any) {
  const event = item.event ?? null
  const status = String(event?.status ?? item.eventStatus ?? '').toUpperCase()
  if (status === 'CANCELLED') return '취소'
  if (status === 'COMPLETED') return '종료'
  const eventDate = new Date(item.eventDate)
  return eventDate < new Date() ? '종료' : '예정'
}

function getMineStatusStyle(item: any) {
  const label = getMineStatusLabel(item)
  return label === '종료' ? 'text-slate-400' : 'text-[var(--accent)]'
}

function startOfDay(date: Date) {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate())
}
