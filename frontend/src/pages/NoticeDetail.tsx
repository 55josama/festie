import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { deleteNotice, getNotice } from '../api/notices'
import { useAuthStore } from '../store/authStore'

function sanitizeHtml(input: string) {
  if (typeof window === 'undefined') return input
  const parser = new DOMParser()
  const doc = parser.parseFromString(`<div>${input}</div>`, 'text/html')
  const wrapper = document.createElement('div')
  wrapper.innerHTML = doc.body.firstElementChild?.innerHTML ?? ''
  wrapper.querySelectorAll('script,style,iframe,object,embed').forEach((node) => node.remove())
  wrapper.querySelectorAll('*').forEach((node) => {
    const el = node as HTMLElement
    Array.from(el.attributes).forEach((attr) => {
      if (el.tagName === 'A' && attr.name === 'href') return
      if (['B', 'STRONG', 'I', 'EM', 'U', 'S', 'STRIKE', 'BR', 'P', 'DIV', 'UL', 'OL', 'LI', 'A', 'SPAN'].includes(el.tagName)) return
      el.removeAttribute(attr.name)
    })
    if (el.tagName === 'A') {
      const href = el.getAttribute('href') ?? ''
      if (!(href.startsWith('http://') || href.startsWith('https://') || href.startsWith('/') || href.startsWith('#'))) {
        el.removeAttribute('href')
      } else {
        el.setAttribute('rel', 'noreferrer noopener')
        el.setAttribute('target', '_blank')
      }
    }
  })
  return wrapper.innerHTML
}

export default function NoticeDetail() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { noticeId = '' } = useParams()
  const { user } = useAuthStore()
  const canWriteNotice = !!user && (user.role === 'ADMIN' || user.role === 'MANAGER' || /_MANAGER$/.test(String(user.role ?? '')))
  const { data: notice } = useQuery({
    queryKey: ['notice', noticeId],
    queryFn: () => getNotice(noticeId),
    enabled: !!noticeId,
  })

  const deleteNoticeMutation = useMutation({
    mutationFn: () => deleteNotice(noticeId),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['notices'] })
      navigate('/notices')
    },
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
        <div className="flex items-start justify-between gap-3">
          <div>
            <div className="text-[11px] font-semibold uppercase tracking-[0.12em] text-slate-400">
              Title
            </div>
            <h1 className="mt-2 text-[26px] font-black tracking-tight text-slate-950">{notice.title}</h1>
          </div>
          {canWriteNotice && (
            <div className="flex items-center gap-2">
              <Link
                to={`/notices/${notice.noticeId}/edit`}
                className="rounded-full border border-violet-200 bg-violet-50 px-4 py-2 text-xs font-semibold text-violet-700"
              >
                수정
              </Link>
              <button
                type="button"
                onClick={() => {
                  if (window.confirm('이 공지를 삭제할까요?')) {
                    deleteNoticeMutation.mutate()
                  }
                }}
                disabled={deleteNoticeMutation.isPending}
                className="rounded-full border border-rose-200 bg-rose-50 px-4 py-2 text-xs font-semibold text-rose-700"
              >
                {deleteNoticeMutation.isPending ? '삭제 중...' : '삭제'}
              </button>
            </div>
          )}
        </div>
        <div className="mt-5 border-t border-[var(--line)] pt-5">
          <div className="text-[11px] font-semibold uppercase tracking-[0.12em] text-slate-400">
            Content
          </div>
          <div
            className="prose prose-slate mt-3 max-w-none text-[15px] leading-7 text-slate-700"
            dangerouslySetInnerHTML={{ __html: sanitizeHtml(notice.content ?? '') }}
          />
        </div>
      </section>
    </div>
  )
}
