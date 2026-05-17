import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { getNotices, type NoticeItem } from '../api/notices'
import { useAuthStore } from '../store/authStore'

export default function Notices() {
  const navigate = useNavigate()
  const { user } = useAuthStore()
  const canWriteNotice = !!user && (user.role === 'ADMIN' || user.role === 'MANAGER' || /_MANAGER$/.test(String(user.role ?? '')))
  const [page, setPage] = useState(0)
  const [pageSize] = useState(10)

  const { data: noticePage = { content: [], page: 0, size: 0, totalElements: 0, totalPages: 0 } } = useQuery({
    queryKey: ['notices', page, pageSize],
    queryFn: () => getNotices({ page, size: pageSize }),
  })

  const noticeItems = useMemo(() => noticePage.content ?? [], [noticePage.content])

  return (
    <div className="space-y-4 px-5 py-5 md:px-8 md:py-7">
      <section className="rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-6">
        <div className="flex items-center justify-between gap-3">
          <div>
            <div className="inline-flex rounded-full bg-[var(--accent-soft)] px-3 py-1 text-[11px] font-semibold text-[var(--accent)]">공지</div>
            <h1 className="mt-3 text-[24px] font-black tracking-tight text-slate-950">Festie 공지사항</h1>
            <p className="mt-1 text-sm text-slate-500">운영팀 공지를 목록 형태로 확인해보세요.</p>
          </div>
          {canWriteNotice && (
            <button
              type="button"
              onClick={() => navigate('/notices/new')}
              className="rounded-full bg-[var(--accent-soft)] px-4 py-2 text-xs font-semibold text-[var(--accent)] hover:bg-white"
            >
              공지 작성
            </button>
          )}
        </div>
      </section>

      <section className="rounded-[24px] border border-[var(--line)] bg-white p-4 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-5">
        {noticeItems.length ? (
          <div className="divide-y divide-[var(--line)]">
            {noticeItems.map((notice) => (
              <NoticeRow key={notice.noticeId} notice={notice} />
            ))}
          </div>
        ) : (
          <div className="rounded-[20px] border border-dashed border-[var(--line)] bg-slate-50 px-4 py-8 text-center text-sm text-slate-500">
            아직 등록된 공지가 없어요.
          </div>
        )}

        {noticePage.totalPages > 1 && (
          <div className="mt-5 flex items-center justify-center gap-2">
            <button
              type="button"
              disabled={page <= 0}
              onClick={() => setPage((prev) => Math.max(0, prev - 1))}
              className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:opacity-40"
            >
              이전
            </button>
            <div className="flex items-center gap-1">
              {Array.from({ length: noticePage.totalPages }, (_, idx) => idx).map((idx) => (
                <button
                  key={idx}
                  type="button"
                  onClick={() => setPage(idx)}
                  className={`h-8 min-w-8 rounded-full px-2 text-xs font-semibold transition-colors ${
                    page === idx ? 'bg-[var(--accent-soft)] text-[var(--accent)]' : 'bg-slate-50 text-slate-600 hover:bg-slate-100'
                  }`}
                >
                  {idx + 1}
                </button>
              ))}
            </div>
            <button
              type="button"
              disabled={page >= noticePage.totalPages - 1}
              onClick={() => setPage((prev) => Math.min(noticePage.totalPages - 1, prev + 1))}
              className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:opacity-40"
            >
              다음
            </button>
          </div>
        )}
      </section>
    </div>
  )
}

function NoticeRow({ notice }: { notice: NoticeItem }) {
  return (
    <Link
      to={`/notices/${notice.noticeId}`}
      className="flex items-start justify-between gap-4 py-4 transition-colors hover:bg-slate-50"
    >
      <div className="min-w-0 flex-1 space-y-1">
        <div className="flex flex-wrap items-center gap-2">
          <span className="rounded-full bg-[var(--accent-soft)] px-2.5 py-1 text-[11px] font-semibold text-[var(--accent)]">공지</span>
        </div>
        <div className="truncate text-[15px] font-semibold leading-6 text-slate-950">{notice.title}</div>
        <div className="line-clamp-2 whitespace-pre-wrap break-words text-[13px] leading-6 text-slate-600">{notice.content}</div>
      </div>
      <div className="shrink-0 text-right text-[11px] text-slate-400">
        <div>상세 보기</div>
      </div>
    </Link>
  )
}
