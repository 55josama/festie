import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { createPost, getCategories, getPost, updatePost } from '../api/community'
import { createEventRequest, getEvents } from '../api/events'
import { createOperationRequest, getMyOperationRequest, getOperationRequest, updateOperationRequest } from '../api/requests'
import { getErrorMessage } from '../lib/error'
import { useAuthStore } from '../store/authStore'

export default function CommunityWrite() {
  const navigate = useNavigate()
  const { postId = '' } = useParams()
  const isEditMode = !!postId
  const [searchParams] = useSearchParams()
  const { user } = useAuthStore()
  const canCreateRequest = !!user
  const requestMode = useMemo<'event' | 'operation' | null>(() => {
    const nextKind = searchParams.get('requestKind')
    if (nextKind === 'event' || nextKind === 'operation') return nextKind
    if (searchParams.get('category') === 'request') {
      return searchParams.get('requestType') === 'event' ? 'event' : 'operation'
    }
    return null
  }, [searchParams])
  const requestEditId = searchParams.get('requestId') ?? ''
  const isOperationEditMode = requestMode === 'operation' && !!requestEditId
  const { data: categories = [] } = useQuery({ queryKey: ['categories'], queryFn: getCategories })
  const { data: requestEvents = [] } = useQuery({
    queryKey: ['request-form', 'event-categories'],
    queryFn: () => getEvents({ size: 100 }),
    enabled: (searchParams.get('requestKind') ?? (searchParams.get('category') === 'request' ? 'event' : '') ) === 'event',
  })
  const { data: editingPost } = useQuery({
    queryKey: ['post', postId],
    queryFn: () => getPost(postId),
    enabled: isEditMode,
  })

  const { data: editingOperationRequest } = useQuery({
    queryKey: ['operation-request', requestEditId, user?.role],
    queryFn: () => user?.role === 'ADMIN'
      ? getOperationRequest(requestEditId)
      : getMyOperationRequest(requestEditId),
    enabled: isOperationEditMode && !!user,
  })

  const [form, setForm] = useState({ title: '', content: '', categoryId: '' })
  const [visibility, setVisibility] = useState<'public' | 'private'>('public')
  const [requestForm, setRequestForm] = useState({
    categoryId: '',
    eventName: '',
    eventDate: '',
    eventPlace: '',
    eventLink: '',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [initialized, setInitialized] = useState(false)

  const requestEventCategories = useMemo(() => {
    const seen = new Map<string, { id: string; name: string }>()
    for (const event of requestEvents as any[]) {
      if (!event?.categoryId || !event?.categoryName) continue
      if (!seen.has(event.categoryId)) {
        seen.set(event.categoryId, { id: event.categoryId, name: event.categoryName })
      }
    }
    return [...seen.values()]
  }, [requestEvents])

  useEffect(() => {
    if (!isEditMode) {
      const nextCategory = searchParams.get('category')
      if (nextCategory === 'request' || requestMode) {
        setVisibility('private')
      }
    }
  }, [isEditMode, requestMode, searchParams])

  useEffect(() => {
    if (!isEditMode || initialized || !editingPost) return
    setForm({
      title: editingPost.title.replace(/^\[행사 요청\]\s*/, ''),
      content: editingPost.content,
      categoryId: editingPost.categoryId,
    })
    setVisibility(editingPost.visibility === 'PRIVATE' ? 'private' : 'public')
    setInitialized(true)
  }, [editingPost, initialized, isEditMode])

  useEffect(() => {
    if (!isOperationEditMode || initialized || !editingOperationRequest) return
    setForm({
      title: editingOperationRequest.title,
      content: editingOperationRequest.content,
      categoryId: '',
    })
    setVisibility('private')
    setInitialized(true)
  }, [editingOperationRequest, initialized, isOperationEditMode])

  useEffect(() => {
    if (requestMode !== 'event' || requestForm.categoryId || requestEventCategories.length === 0) return
    setRequestForm((prev) => ({ ...prev, categoryId: requestEventCategories[0].id }))
  }, [requestEventCategories, requestForm.categoryId, requestMode])

  const submit = async (e: FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      if (requestMode === 'event') {
        const requestDescription = [
          form.content.trim(),
          requestForm.eventName ? `행사명: ${requestForm.eventName}` : '',
          requestForm.eventDate ? `일정: ${requestForm.eventDate}` : '',
          requestForm.eventPlace ? `장소: ${requestForm.eventPlace}` : '',
          requestForm.eventLink ? `링크: ${requestForm.eventLink}` : '',
        ].filter(Boolean).join('\n')
        const requestCategoryId = requestForm.categoryId || requestEventCategories[0]?.id || ''
        if (!requestCategoryId) {
          throw new Error('event-category-required')
        }
        await createEventRequest({
          title: form.title,
          categoryId: requestCategoryId,
          link: requestForm.eventLink,
          description: requestDescription,
        })
        navigate('/community?tab=requests&requestKind=event')
        return
      }

      if (requestMode === 'operation') {
        if (requestEditId) {
          await updateOperationRequest(requestEditId, {
            title: form.title,
            content: form.content,
          })
          navigate('/community')
          return
        }
        await createOperationRequest({
          title: form.title,
          content: form.content,
        })
        navigate('/community?tab=requests&requestKind=operation')
        return
      }

      const payload: {
        categoryId: string
        title: string
        content: string
        visibility: 'PUBLIC' | 'PRIVATE'
      } = {
        categoryId: form.categoryId,
        title: form.title,
        content: form.content,
        visibility: visibility === 'private' ? 'PRIVATE' : 'PUBLIC',
      }

      const result = isEditMode ? await updatePost(postId, payload) : await createPost(payload)
      navigate(`/community/${result.id}`)
    } catch (err: any) {
      if (err instanceof Error && err.message === 'event-category-required') {
        setError('행사 카테고리를 선택해주세요.')
        return
      }
      setError(getErrorMessage(err, '글을 저장하지 못했습니다.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="px-5 py-6 md:px-8 md:py-8">
      <div className="mx-auto max-w-3xl rounded-[30px] border border-[var(--line)] bg-white p-6 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-8">
        {requestMode && !canCreateRequest ? (
          <div className="space-y-4">
            <div className="inline-flex rounded-full bg-[var(--accent-soft)] px-3 py-1 text-sm font-semibold text-[var(--accent)]">요청 작성</div>
            <div className="rounded-[24px] border border-[var(--line)] bg-slate-50 p-5 text-sm text-slate-600">
              요청 작성은 일반 사용자만 가능합니다.
            </div>
            <div className="flex justify-end">
              <button
                type="button"
                onClick={() => navigate('/community')}
                className="rounded-full bg-[var(--accent-soft)] px-5 py-3 text-sm font-semibold text-[var(--accent)]"
              >
                커뮤니티로 돌아가기
              </button>
            </div>
          </div>
        ) : (
          <>
        <div className="mb-6">
          <div className="inline-flex rounded-full bg-[var(--accent-soft)] px-3 py-1 text-sm font-semibold text-[var(--accent)]">글쓰기</div>
          <h1 className="mt-4 text-[32px] font-black tracking-tight text-slate-950">
            {requestMode === 'event'
              ? '이벤트 요청을 남겨보세요'
              : requestMode === 'operation'
                ? requestEditId
                  ? '운영 요청을 수정해보세요'
                  : '운영 요청을 남겨보세요'
                : isEditMode
                  ? '게시글을 수정해보세요'
                  : '공연을 본 사람의 시선으로 남겨보세요'}
          </h1>
          <p className="mt-2 text-sm text-slate-500">
            {requestMode === 'event'
              ? '행사 카테고리와 요청 내용을 함께 정리할 수 있어요.'
              : requestMode === 'operation'
                ? requestEditId
                  ? '운영 요청 내용을 다시 정리할 수 있어요.'
                  : '운영 요청 내용을 간단하게 정리할 수 있어요.'
                : isEditMode
                  ? '제목, 카테고리, 내용을 다시 정리할 수 있어요.'
                  : '후기, 꿀팁, 요청 글을 빠르게 작성할 수 있어요.'}
          </p>
        </div>

        <form onSubmit={submit} className="space-y-5">
          {!requestMode ? (
            <label className="block">
              <div className="mb-2 text-sm font-medium text-slate-700">카테고리</div>
              <select
                value={form.categoryId}
                onChange={(e) => setForm((prev) => ({ ...prev, categoryId: e.target.value }))}
                className="w-full rounded-full border border-[var(--line)] bg-slate-50 px-4 py-3 text-sm outline-none"
              >
                <option value="">선택하세요</option>
                {categories
                  .filter((category: any) => category.name !== '요청')
                  .map((category: any) => (
                    <option key={category.id} value={category.id}>
                      {category.name}
                    </option>
                  ))}
              </select>
            </label>
          ) : (
            <div className="space-y-3 rounded-[22px] border border-[var(--line)] bg-slate-50 p-4">
              <div className="text-sm font-semibold text-slate-700">
                {requestMode === 'event' ? '이벤트 요청 폼' : '운영 요청 폼'}
              </div>
              {requestMode === 'event' && (
                <label className="block">
                  <div className="mb-2 text-sm font-medium text-slate-700">행사 카테고리</div>
                  <select
                    value={requestForm.categoryId}
                    onChange={(e) => setRequestForm((prev) => ({ ...prev, categoryId: e.target.value }))}
                    className="w-full rounded-full border border-[var(--line)] bg-white px-4 py-3 text-sm outline-none"
                  >
                    <option value="">행사 카테고리 선택</option>
                    {requestEventCategories.map((category: any) => (
                      <option key={category.id} value={category.id}>
                        {category.name}
                      </option>
                    ))}
                  </select>
                </label>
              )}
              {requestMode === 'event' && (
                <>
                  <input
                    value={form.title}
                    onChange={(e) => setForm((prev) => ({ ...prev, title: e.target.value }))}
                    placeholder="이벤트 요청 제목"
                    className="w-full rounded-full border border-[var(--line)] bg-white px-4 py-3 text-sm outline-none"
                  />
                  <div className="rounded-[20px] border border-[var(--line)] bg-white p-4">
                    <div className="mb-3 text-sm font-semibold text-slate-700">상세 내용</div>
                    <textarea
                      value={form.content}
                      onChange={(e) => setForm((prev) => ({ ...prev, content: e.target.value }))}
                      className="min-h-48 w-full rounded-[24px] border border-[var(--line)] bg-slate-50 p-4 text-sm outline-none"
                    />
                  </div>
                  <input
                    value={requestForm.eventName}
                    onChange={(e) => setRequestForm((prev) => ({ ...prev, eventName: e.target.value }))}
                    placeholder="행사명"
                    className="w-full rounded-full border border-[var(--line)] bg-white px-4 py-3 text-sm outline-none"
                  />
                  <input
                    value={requestForm.eventDate}
                    onChange={(e) => setRequestForm((prev) => ({ ...prev, eventDate: e.target.value }))}
                    placeholder="예정 날짜 (예: 2026-12-31)"
                    className="w-full rounded-full border border-[var(--line)] bg-white px-4 py-3 text-sm outline-none"
                  />
                  <input
                    value={requestForm.eventPlace}
                    onChange={(e) => setRequestForm((prev) => ({ ...prev, eventPlace: e.target.value }))}
                    placeholder="장소"
                    className="w-full rounded-full border border-[var(--line)] bg-white px-4 py-3 text-sm outline-none"
                  />
                  <input
                    value={requestForm.eventLink}
                    onChange={(e) => setRequestForm((prev) => ({ ...prev, eventLink: e.target.value }))}
                    placeholder="참고 링크"
                    className="w-full rounded-full border border-[var(--line)] bg-white px-4 py-3 text-sm outline-none"
                  />
                </>
              )}
              {requestMode === 'operation' && (
                <>
                  <input
                    value={form.title}
                    onChange={(e) => setForm((prev) => ({ ...prev, title: e.target.value }))}
                    placeholder="운영 요청 제목"
                    className="w-full rounded-full border border-[var(--line)] bg-white px-4 py-3 text-sm outline-none"
                  />
                  <div className="rounded-[20px] border border-[var(--line)] bg-white p-4">
                    <div className="mb-3 text-sm font-semibold text-slate-700">상세 내용</div>
                    <textarea
                      value={form.content}
                      onChange={(e) => setForm((prev) => ({ ...prev, content: e.target.value }))}
                      className="min-h-48 w-full rounded-[24px] border border-[var(--line)] bg-slate-50 p-4 text-sm outline-none"
                    />
                  </div>
                </>
              )}
            </div>
          )}

          {!requestMode && (
            <label className="block">
              <div className="mb-2 text-sm font-medium text-slate-700">제목</div>
              <input
                value={form.title}
                onChange={(e) => setForm((prev) => ({ ...prev, title: e.target.value }))}
                className="w-full rounded-full border border-[var(--line)] bg-slate-50 px-4 py-3 text-sm outline-none"
              />
            </label>
          )}
          {!requestMode && (
            <label className="block">
              <div className="mb-2 text-sm font-medium text-slate-700">내용</div>
              <textarea
                value={form.content}
                onChange={(e) => setForm((prev) => ({ ...prev, content: e.target.value }))}
                className="min-h-48 w-full rounded-[24px] border border-[var(--line)] bg-slate-50 p-4 text-sm outline-none"
              />
            </label>
          )}

          {error && <div className="rounded-2xl bg-rose-50 px-4 py-3 text-sm text-rose-700">{error}</div>}

          <div className="flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={() => (isEditMode ? navigate(-1) : navigate('/community'))}
              className="rounded-full bg-slate-100 px-5 py-3 text-sm font-semibold text-slate-600 hover:bg-slate-200"
            >
              취소
            </button>
            <button disabled={loading} className="rounded-full bg-[var(--accent-soft)] px-5 py-3 text-sm font-semibold text-[var(--accent)] transition-colors hover:bg-slate-100">
              {loading ? '저장 중...' : isEditMode ? '수정하기' : '등록하기'}
            </button>
          </div>
        </form>
          </>
        )}
      </div>
    </div>
  )
}
