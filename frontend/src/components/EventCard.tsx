import { Link } from 'react-router-dom'
import type { Event } from '../types'
import { formatDateRange, formatPrice, getDDay } from '../lib/format'

const CATEGORY_ACCENT: Record<string, string> = {
  concert: 'from-violet-100 to-violet-50 text-violet-700',
  festival: 'from-fuchsia-100 to-fuchsia-50 text-fuchsia-700',
  fanmeeting: 'from-pink-100 to-pink-50 text-pink-700',
  popup: 'from-sky-100 to-sky-50 text-sky-700',
}

const CATEGORY_CHIP_CLASS: Record<string, string> = {
  concert: 'bg-violet-100 text-violet-700',
  festival: 'bg-fuchsia-100 text-fuchsia-700',
  fanmeeting: 'bg-pink-100 text-pink-700',
  popup: 'bg-sky-100 text-sky-700',
}

const STATUS_LABEL: Record<string, string> = {
  SCHEDULED: '예정',
  IN_PROGRESS: '진행중',
  COMPLETED: '종료',
  CANCELLED: '취소',
}

export default function EventCard({ event }: { event: Event }) {
  const dDay = getDDay(event.startAt)
  const price = formatPrice(event.minFee, event.maxFee)
  const categoryKey = normalizeCategoryKey(event.categoryName)
  const accent = CATEGORY_ACCENT[normalizeCategoryKey(event.categoryName)] ?? 'from-slate-100 to-slate-50 text-slate-700'
  const chipClass = CATEGORY_CHIP_CLASS[categoryKey] ?? 'bg-violet-100 text-violet-700'

  return (
    <Link
      to={`/events/${event.id}`}
      className="group block overflow-hidden rounded-[24px] border border-[var(--line)] bg-white transition-all hover:-translate-y-0.5 hover:border-slate-300 hover:shadow-[0_16px_34px_rgba(15,23,42,0.08)]"
    >
      <div className={`bg-gradient-to-br ${accent} flex h-44 items-end justify-between px-5 pb-4`}>
        <div>
          <div className={`mb-2 inline-flex rounded-full px-3 py-1 text-xs font-semibold ${chipClass}`}>
            {displayEventCategoryLabel(event.categoryName)}
          </div>
          <div className="text-xs text-slate-500">{STATUS_LABEL[event.status] ?? event.status}</div>
        </div>
        <div className="rounded-full bg-white/85 px-3 py-1 text-xs font-semibold text-slate-700 shadow-sm">
          {dDay ?? '티켓팅'}
        </div>
      </div>
      <div className="space-y-2 px-5 py-4">
        <h3 className="line-clamp-1 text-[17px] font-semibold tracking-tight text-slate-950">{event.name}</h3>
        <p className="text-[14px] text-slate-500">{formatDateRange(event.startAt, event.endAt)}</p>
        <p className="line-clamp-1 text-[14px] text-slate-500">{event.place}</p>
        <div className="flex items-center justify-between pt-1">
          <span className="text-[14px] font-medium text-slate-700">{price}</span>
          <span className="text-[13px] font-medium text-[var(--accent)]">자세히 보기</span>
        </div>
      </div>
    </Link>
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

function displayEventCategoryLabel(name: string) {
  const key = normalizeCategoryKey(name)
  return {
    concert: '콘서트',
    festival: '축제',
    fanmeeting: '팬미팅',
    popup: '팝업스토어',
  }[key] ?? (String(name ?? '').trim().toUpperCase() || 'EVENT')
}
