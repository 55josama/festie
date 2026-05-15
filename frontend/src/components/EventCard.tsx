import { Link } from 'react-router-dom'
import type { Event } from '../types'
import { formatDateRange, formatPrice, getDDay } from '../lib/format'

const CATEGORY_ACCENT: Record<string, string> = {
  concert: 'from-sky-100 to-sky-50 text-sky-700',
  festival: 'from-emerald-100 to-emerald-50 text-emerald-700',
  fanmeeting: 'from-fuchsia-100 to-fuchsia-50 text-fuchsia-700',
  popup: 'from-sky-100 to-sky-50 text-sky-700',
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
  const accent = CATEGORY_ACCENT[normalizeCategoryKey(event.categoryName)] ?? 'from-slate-100 to-slate-50 text-slate-700'

  return (
    <Link
      to={`/events/${event.id}`}
      className="group block overflow-hidden rounded-[24px] border border-[var(--line)] bg-white transition-all hover:-translate-y-0.5 hover:border-slate-300 hover:shadow-[0_16px_34px_rgba(15,23,42,0.08)]"
    >
      <div className={`bg-gradient-to-br ${accent} flex h-44 items-end justify-between px-5 pb-4`}>
        <div>
          <div className="mb-2 inline-flex rounded-full bg-white/80 px-3 py-1 text-xs font-semibold text-slate-700">
            {event.categoryName}
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
  if (name === '\uCF58\uC11C\uD2B8') return 'concert'
  if (name === '\uCD95\uC81C') return 'festival'
  if (name === '\uD32C\uBBF8\uD305') return 'fanmeeting'
  if (name === '\uD31D\uC5C5\uC2A4\uD1A0\uC5B4') return 'popup'
  return name.toLowerCase()
}
