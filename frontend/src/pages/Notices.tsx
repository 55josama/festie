import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getNotices, type NoticeItem } from '../api/notices'

export default function Notices() {
  const { data: page = { content: [], page: 0, size: 0, totalElements: 0, totalPages: 0 } } = useQuery({
    queryKey: ['notices'],
    queryFn: () => getNotices({ page: 0, size: 20 }),
  })

  return (
    <div className="space-y-4 px-5 py-5 md:px-8 md:py-7">
      <section className="rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-6">
        <div className="flex items-center justify-between gap-3">
          <div>
            <div className="inline-flex rounded-full bg-[var(--accent-soft)] px-3 py-1 text-[11px] font-semibold text-[var(--accent)]">공지</div>
            <h1 className="mt-3 text-[24px] font-black tracking-tight text-slate-950">Festie 공지사항</h1>
            <p className="mt-1 text-sm text-slate-500">운영팀 공지를 한눈에 확인해보세요.</p>
          </div>
        </div>
      </section>

      <section className="space-y-3">
        {page.content.length ? page.content.map((notice) => (
          <NoticeCard key={notice.noticeId} notice={notice} />
        )) : (
          <div className="rounded-[20px] border border-dashed border-[var(--line)] bg-white px-4 py-8 text-center text-sm text-slate-500">
            아직 등록된 공지가 없어요.
          </div>
        )}
      </section>
    </div>
  )
}

function NoticeCard({ notice }: { notice: NoticeItem }) {
  return (
    <Link
      to={`/notices/${notice.noticeId}`}
      className="block rounded-[20px] border border-[var(--line)] bg-slate-50 p-4 transition-colors hover:bg-white md:p-5"
    >
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0 space-y-2">
          <div className="flex flex-wrap items-center gap-2">
            <span className="rounded-full bg-[var(--accent-soft)] px-2.5 py-1 text-[11px] font-semibold text-[var(--accent)]">공지</span>
          </div>
          <div className="truncate text-[16px] font-bold leading-6 text-slate-950">{notice.title}</div>
          <div className="line-clamp-3 whitespace-pre-wrap break-words text-[13px] leading-6 text-slate-600">{notice.content}</div>
        </div>
        <div className="shrink-0 text-right text-[11px] text-slate-400">
          <div>상세 보기</div>
        </div>
      </div>
    </Link>
  )
}
