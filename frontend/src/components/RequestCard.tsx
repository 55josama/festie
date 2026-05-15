import { type ReactNode } from 'react'
import { formatDateTime } from '../lib/format'

type RequestCardProps = {
  kindLabel: string
  title: string
  body: string
  authorNickname?: string | null
  createdAt?: string | null
  status: string
  statusLabel?: string
  statusClassName?: string
  kindClassName?: string
  metaLabel?: string | null
  rejectReason?: string | null
  actions?: ReactNode
}

export default function RequestCard({
  kindLabel,
  title,
  body,
  authorNickname,
  createdAt,
  status,
  statusLabel,
  statusClassName,
  kindClassName,
  metaLabel,
  rejectReason,
  actions,
}: RequestCardProps) {
  const hasTitle = Boolean(String(title ?? '').trim())
  const hasAuthor = Boolean(String(authorNickname ?? '').trim())
  return (
    <article className="rounded-[20px] border border-[var(--line)] bg-slate-50 p-4 transition-colors hover:bg-white md:p-5">
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0 flex-1 space-y-2">
          <div className="flex flex-wrap items-center gap-2">
            <span className={`rounded-full px-2.5 py-1 text-[11px] font-semibold ${kindClassName ?? 'bg-[var(--accent-soft)] text-[var(--accent)]'}`}>
              {kindLabel}
            </span>
            {metaLabel && (
              <span className="rounded-full bg-white px-2.5 py-1 text-[11px] font-semibold text-slate-500">
                {metaLabel}
              </span>
            )}
            {hasTitle && (
              <div className="min-w-0 flex-1">
                <div className="truncate text-[15px] font-semibold leading-6 text-slate-950">{title}</div>
              </div>
            )}
          </div>

          <div className="flex flex-wrap items-center gap-2 text-[12px] text-slate-500">
            {hasAuthor && <span>{authorNickname}</span>}
            {hasAuthor && <span>·</span>}
            <span>{formatDateTime(createdAt)}</span>
          </div>

          <div className="line-clamp-2 whitespace-pre-wrap break-words text-[13px] leading-6 text-slate-600">
            {body}
          </div>

          {rejectReason && status !== 'PENDING' && (
            <div className="rounded-[16px] border border-rose-200 bg-rose-50 px-3 py-2 text-[12px] leading-5 text-rose-700">
              <span className="font-semibold">반려 사유</span>
              <div className="mt-1 whitespace-pre-wrap break-words">{rejectReason}</div>
            </div>
          )}

          {actions && (
            <div className="pt-2">
              {actions}
            </div>
          )}
        </div>

        <div className="shrink-0 text-right">
          <span className={`rounded-full px-2.5 py-1 text-[11px] font-semibold ${statusClassName ?? 'bg-slate-100 text-slate-600'}`}>
            {statusLabel ?? status}
          </span>
        </div>
      </div>
    </article>
  )
}
