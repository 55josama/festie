import { Link } from 'react-router-dom'
import type { Post } from '../types'

export default function PostCard({ post }: { post: Post }) {
  const chipClass = (
    {
      review: 'bg-violet-100 text-violet-700',
      tip: 'bg-sky-100 text-sky-700',
      free: 'bg-emerald-100 text-emerald-700',
      request: 'bg-rose-100 text-rose-700',
    } as Record<string, string>
  )[normalizePostCategoryKey(post.categoryName)] ?? 'bg-[var(--accent-soft)] text-[var(--accent)]'

  return (
    <Link
      to={`/community/${post.id}`}
      className="flex flex-col gap-3 rounded-[18px] border border-[var(--line)] bg-slate-50 px-4 py-3 hover:bg-white md:flex-row md:items-start md:justify-between"
    >
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <span className={`rounded-full px-2.5 py-1 text-[11px] font-semibold ${chipClass}`}>
            {post.categoryName}
          </span>
          {post.visibility === 'PRIVATE' && (
            <span className="rounded-full bg-slate-200 px-2.5 py-1 text-[11px] font-semibold text-slate-600">
              비공개
            </span>
          )}
          {post.eventName && <span className="text-[11px] text-slate-500">{post.eventName}</span>}
        </div>
        <div className="mt-2 truncate text-sm font-semibold text-slate-950">{post.title}</div>
        <div className="mt-1 truncate text-xs text-slate-500">
          {post.authorNickname ?? '익명'} · {post.createdAt}
        </div>
      </div>
      <div className="flex shrink-0 items-center justify-between text-xs text-slate-500 md:block md:text-right">
        <div>♡ {post.likeCount}</div>
        <div>💬 {post.commentCount}</div>
      </div>
    </Link>
  )
}

function normalizePostCategoryKey(name: string) {
  if (name === '\uD6C4\uAE30') return 'review'
  if (name === '\uAFC0\uD301') return 'tip'
  if (name === '\uC790\uC720') return 'free'
  if (name === '\uC694\uCCAD') return 'request'
  return name.toLowerCase()
}
