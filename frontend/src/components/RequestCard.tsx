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
  statusActions?: ReactNode
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
  statusActions,
  actions,
}: RequestCardProps) {
  const hasTitle = Boolean(String(title ?? '').trim())
  const hasAuthor = Boolean(String(authorNickname ?? '').trim())
  const hasCreatedAt = Boolean(String(createdAt ?? '').trim())
  return (
    <article className="rounded-[20px] border border-[var(--line)] bg-slate-50 p-4 transition-colors hover:bg-white md:p-5">
      <div className="grid gap-4 lg:grid-cols-[minmax(0,3fr)_minmax(260px,2fr)] lg:items-start">
        <div className="min-w-0 space-y-2">
          <div className="flex flex-wrap items-center gap-2">
            <span className={`rounded-full px-2.5 py-1 text-[11px] font-semibold ${kindClassName ?? 'bg-[var(--accent-soft)] text-[var(--accent)]'}`}>
              {kindLabel}
            </span>
            {metaLabel && (
              <span className="rounded-full bg-white px-2.5 py-1 text-[11px] font-semibold text-slate-500">
                {metaLabel}
              </span>
            )}
            <span className={`rounded-full px-2.5 py-1 text-[11px] font-semibold ${statusClassName ?? 'bg-slate-100 text-slate-600'}`}>
              {statusLabel ?? status}
            </span>
          </div>

          {hasTitle && (
            <div className="min-w-0">
              <div className="line-clamp-2 break-words text-[15px] font-bold leading-6 text-slate-950">
                {title}
              </div>
            </div>
          )}

          {(hasAuthor || hasCreatedAt) && (
            <div className="flex flex-wrap items-center gap-2 text-[12px] text-slate-500">
              {hasAuthor && <span>{authorNickname}</span>}
              {hasAuthor && hasCreatedAt && <span>·</span>}
              {hasCreatedAt && <span>{formatDateTime(createdAt)}</span>}
            </div>
          )}

          <div className="line-clamp-3 whitespace-pre-wrap break-words text-[13px] leading-6 text-slate-600">
            {body}
          </div>

          {rejectReason && status !== 'PENDING' && (
            <div
              className={`rounded-[16px] border px-3 py-2 text-[12px] leading-5 ${
                status === 'REJECTED'
                  ? 'border-rose-200 bg-rose-50 text-rose-700'
                  : 'border-emerald-200 bg-emerald-50 text-emerald-700'
              }`}
            >
              <span className="font-semibold">
                {status === 'REJECTED' ? '반려 사유' : '관리자 메모'}
              </span>
              <div className="mt-1 whitespace-pre-wrap break-words">{rejectReason}</div>
            </div>
          )}

          <div className="flex flex-wrap items-center gap-2 pt-2">
            {statusActions}
          </div>

        </div>

        {actions && (
          <div className="flex shrink-0 flex-col gap-2 rounded-[18px] border border-[var(--line)] bg-white p-4">
            {actions}
          </div>
        )}
      </div>
    </article>
  )
}
