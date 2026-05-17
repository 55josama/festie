import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'
import { getNotice } from '../api/notices'

export default function NoticeDetail() {
  const { noticeId = '' } = useParams()
  const { data: notice } = useQuery({
    queryKey: ['notice', noticeId],
    queryFn: () => getNotice(noticeId),
    enabled: !!noticeId,
  })

  if (!notice) {
    return <div className="px-5 py-10 text-slate-500">공지사항을 불러오는 중입니다.</div>
  }

  return (
    <div className="px-5 py-5 md:px-8 md:py-7">
      <div className="mb-4 flex items-center gap-2 text-sm text-slate-500">
        <Link to="/" className="hover:text-slate-900">홈</Link>
        <span>/</span>
        <Link to="/notices" className="hover:text-slate-900">공지</Link>
        <span>/</span>
        <span className="text-slate-900">공지사항</span>
      </div>

      <section className="rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-6">
        <div className="inline-flex rounded-full bg-[var(--accent-soft)] px-3 py-1 text-[11px] font-semibold text-[var(--accent)]">공지</div>
        <h1 className="mt-3 text-[26px] font-black tracking-tight text-slate-950">{notice.title}</h1>
        <div className="mt-4 whitespace-pre-wrap break-words text-[15px] leading-7 text-slate-700">{notice.content}</div>
      </section>
    </div>
  )
}
