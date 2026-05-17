import { useMutation, useQuery } from '@tanstack/react-query'
import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { createNotice, getNotice, updateNotice } from '../api/notices'
import { useAuthStore } from '../store/authStore'

export default function NoticeWrite() {
  const { noticeId = '' } = useParams()
  const navigate = useNavigate()
  const { user } = useAuthStore()
  const canWriteNotice = !!user && (user.role === 'ADMIN' || user.role === 'MANAGER' || /_MANAGER$/.test(String(user.role ?? '')))
  const isEditMode = Boolean(noticeId)
  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')

  const { data: notice } = useQuery({
    queryKey: ['notice', noticeId],
    queryFn: () => getNotice(noticeId),
    enabled: !!noticeId && canWriteNotice,
  })

  useEffect(() => {
    if (!notice || !isEditMode) return
    setTitle(notice.title ?? '')
    setContent(notice.content ?? '')
  }, [isEditMode, notice])

  const saveNoticeMutation = useMutation({
    mutationFn: () => {
      if (isEditMode) {
        return updateNotice(noticeId, title.trim(), content.trim())
      }
      return createNotice(title.trim(), content.trim())
    },
    onSuccess: (savedNotice) => {
      navigate(`/notices/${savedNotice.noticeId}`)
    },
  })

  const submitLabel = useMemo(() => {
    if (saveNoticeMutation.isPending) return isEditMode ? '수정 중...' : '작성 중...'
    return isEditMode ? '공지 수정' : '공지 등록'
  }, [isEditMode, saveNoticeMutation.isPending])

  if (!canWriteNotice) {
    return (
      <div className="px-5 py-10 text-slate-500">
        공지 작성 권한이 없습니다.
      </div>
    )
  }

  return (
    <div className="space-y-4 px-5 py-5 md:px-8 md:py-7">
      <div className="mb-1 flex items-center gap-2 text-sm text-slate-500">
        <Link to="/" className="hover:text-slate-900">홈</Link>
        <span>/</span>
        <Link to="/notices" className="hover:text-slate-900">공지</Link>
        <span>/</span>
        <span className="text-slate-900">{isEditMode ? '공지 수정' : '공지 작성'}</span>
      </div>

      <section className="rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-6">
        <div className="inline-flex rounded-full bg-[var(--accent-soft)] px-3 py-1 text-[11px] font-semibold text-[var(--accent)]">
          {isEditMode ? '공지 수정' : '공지 작성'}
        </div>
        <h1 className="mt-3 text-[24px] font-black tracking-tight text-slate-950">
          {isEditMode ? '공지사항을 수정해요' : '새 공지사항을 작성해요'}
        </h1>
        <p className="mt-1 text-sm text-slate-500">관리자와 매니저만 작성할 수 있어요.</p>

        <div className="mt-5 space-y-3">
          <input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="공지 제목"
            className="w-full rounded-[18px] border border-[var(--line)] bg-white px-4 py-3 text-sm outline-none"
          />
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="공지 내용을 입력하세요."
            className="min-h-[220px] w-full rounded-[18px] border border-[var(--line)] bg-white px-4 py-3 text-sm leading-6 outline-none"
          />
          <div className="flex items-center justify-end gap-2">
            <Link
              to={isEditMode ? `/notices/${noticeId}` : '/notices'}
              className="rounded-full border border-[var(--line)] bg-white px-4 py-2.5 text-sm font-semibold text-slate-700"
            >
              취소
            </Link>
            <button
              type="button"
              onClick={() => saveNoticeMutation.mutate()}
              disabled={!title.trim() || !content.trim() || saveNoticeMutation.isPending}
              className="rounded-full bg-[var(--accent)] px-5 py-2.5 text-sm font-semibold text-white disabled:opacity-50"
            >
              {submitLabel}
            </button>
          </div>
        </div>
      </section>
    </div>
  )
}
