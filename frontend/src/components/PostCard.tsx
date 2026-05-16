import { Link } from 'react-router-dom'
import type { Post } from '../types'

export default function PostCard({ post, categoryLabel }: { post: Post; categoryLabel?: string }) {
  const label = categoryLabel ?? post.categoryName ?? '카테고리'
  const chipClass = (
    {
      review: 'bg-violet-100 text-violet-700',
      tip: 'bg-sky-100 text-sky-700',
      free: 'bg-emerald-100 text-emerald-700',
      request: 'bg-rose-100 text-rose-700',
    } as Record<string, string>
  )[normalizePostCategoryKey(label)] ?? 'bg-[var(--accent-soft)] text-[var(--accent)]'

  return (
    <Link
      to={`/community/${post.id}`}
      className="flex flex-col gap-4 rounded-[20px] border border-[var(--line)] bg-slate-50 px-5 py-4 hover:bg-white md:flex-row md:items-start md:justify-between md:px-6 md:py-4"
    >
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <span className={`rounded-full px-2.5 py-1 text-[11px] font-semibold ${chipClass}`}>
            {label}
          </span>
          <div className="min-w-0 flex-1">
            <div className="truncate text-[15px] font-semibold leading-6 text-slate-950">{post.title}</div>
          </div>
        </div>
        <div className="mt-2 flex flex-wrap items-center gap-2 text-[12px] text-slate-500">
          {post.visibility === 'PRIVATE' && (
            <span className="rounded-full bg-slate-200 px-2.5 py-1 text-[11px] font-semibold text-slate-600">
              비공개
            </span>
          )}
          {post.eventName && <span>{post.eventName}</span>}
          <span>{post.authorNickname ?? '익명'} · {post.createdAt}</span>
        </div>
      </div>
      <div className="flex shrink-0 items-center justify-between text-[12px] text-slate-500 md:block md:text-right">
        <div>♡ {post.likeCount}</div>
        <div>💬 {post.commentCount}</div>
      </div>
    </Link>
  )
}

function normalizePostCategoryKey(name: string) {
  const normalized = String(name ?? '').trim().toLowerCase()
  if (normalized === '후기' || normalized === 'review') return 'review'
  if (normalized === '꿀팁' || normalized === 'tip') return 'tip'
  if (normalized === '자유' || normalized === 'free') return 'free'
  if (normalized === '요청' || normalized === 'request') return 'request'
  return normalized
}
