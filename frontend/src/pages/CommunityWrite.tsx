import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { createPost, getCategories, getPost, updatePost } from '../api/community'
import { createEventRequest, getEvents } from '../api/events'
import { createOperationRequest } from '../api/requests'

export default function CommunityWrite() {
  const navigate = useNavigate()
  const { postId = '' } = useParams()
  const isEditMode = !!postId
  const [searchParams] = useSearchParams()
  const { data: categories = [] } = useQuery({ queryKey: ['categories'], queryFn: getCategories })
  const { data: eventSources = [] } = useQuery({
    queryKey: ['events', 'request-form-categories'],
    queryFn: () => getEvents({ size: 100 }),
  })
  const { data: editingPost } = useQuery({
    queryKey: ['post', postId],
    queryFn: () => getPost(postId),
    enabled: isEditMode,
  })

  const [form, setForm] = useState({ title: '', content: '', categoryId: '' })
  const [visibility, setVisibility] = useState<'public' | 'private'>('public')
  const [requestType, setRequestType] = useState<'general' | 'event'>('general')
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

  const selectedCategory = useMemo(() => categories.find((category: any) => category.id === form.categoryId), [categories, form.categoryId])
  const isRequestCategory = selectedCategory?.name === '요청'
  const eventCategories = useMemo(() => {
    const seen = new Map<string, { id: string; name: string }>()
    for (const event of eventSources as any[]) {
      const id = String(event.categoryId ?? '').trim()
      const name = String(event.categoryName ?? '').trim()
      if (!id || !name || seen.has(id)) continue
      seen.set(id, { id, name })
    }
    return Array.from(seen.values())
  }, [eventSources])

  useEffect(() => {
    if (!isEditMode) {
      const nextCategory = searchParams.get('category')
      const nextRequestType = searchParams.get('requestType')
      if (nextRequestType === 'event') {
        setRequestType('event')
      }
      if (nextCategory === 'request') {
        const requestCategory = categories.find((category: any) => category.name === '요청')
        if (requestCategory?.id) {
          setForm((prev) => ({
            ...prev,
            categoryId: requestCategory.id,
          }))
          setVisibility('private')
          if (nextRequestType === 'event') {
            setRequestType('event')
          }
        }
      }
    }
  }, [categories, isEditMode, searchParams])

  useEffect(() => {
    if (!isEditMode || initialized || !editingPost) return
    setForm({
      title: editingPost.title.replace(/^\[행사 요청\]\s*/, ''),
      content: editingPost.content,
      categoryId: editingPost.categoryId,
    })
    setVisibility(editingPost.visibility === 'PRIVATE' ? 'private' : 'public')
    setRequestType('general')
    setInitialized(true)
  }, [editingPost, initialized, isEditMode])

  useEffect(() => {
    if (requestType !== 'event' || requestForm.categoryId || eventCategories.length === 0) return
    setRequestForm((prev) => ({ ...prev, categoryId: eventCategories[0].id }))
  }, [eventCategories, requestForm.categoryId, requestType])

  useEffect(() => {
    if (!isRequestCategory) {
      setRequestType('general')
      setVisibility('public')
    } else {
      setVisibility('private')
    }
  }, [isRequestCategory])

  const submit = async (e: FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      const requestDescription =
        isRequestCategory && requestType === 'event'
          ? [
              form.content.trim(),
              requestForm.eventName ? `행사명: ${requestForm.eventName}` : '',
              requestForm.eventDate ? `일정: ${requestForm.eventDate}` : '',
              requestForm.eventPlace ? `장소: ${requestForm.eventPlace}` : '',
              requestForm.eventLink ? `링크: ${requestForm.eventLink}` : '',
            ].filter(Boolean).join('\n')
          : form.content

      const payload: {
        categoryId: string
        title: string
        content: string
        visibility: 'PUBLIC' | 'PRIVATE'
      } = {
        categoryId: form.categoryId,
        title: isRequestCategory && requestType === 'event' ? `[행사 요청] ${form.title}` : form.title,
        content: isRequestCategory ? requestDescription : form.content,
        visibility: isRequestCategory && visibility === 'private' ? 'PRIVATE' : 'PUBLIC',
      }

      if (isRequestCategory && requestType === 'event') {
        const requestCategoryId = requestForm.categoryId || eventCategories[0]?.id || ''
        if (!requestCategoryId) {
          throw new Error('event-category-required')
        }
        await createEventRequest({
          title: form.title,
          categoryId: requestCategoryId,
          link: requestForm.eventLink,
          description: requestDescription,
        })
        navigate('/community')
        return
      }

      if (isRequestCategory && requestType === 'general') {
        await createOperationRequest({
          title: form.title,
          content: form.content,
        })
        navigate('/community')
        return
      }

      const result = isEditMode ? await updatePost(postId, payload) : await createPost(payload)
      navigate(`/community/${result.id}`)
    } catch (err: any) {
      if (err instanceof Error && err.message === 'event-category-required') {
        setError('행사 카테고리를 선택해주세요.')
        return
      }
      setError(err?.response?.data?.message ?? '글을 저장하지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="px-5 py-6 md:px-8 md:py-8">
      <div className="mx-auto max-w-3xl rounded-[30px] border border-[var(--line)] bg-white p-6 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-8">
        <div className="mb-6">
          <div className="inline-flex rounded-full bg-[var(--accent-soft)] px-3 py-1 text-sm font-semibold text-[var(--accent)]">글쓰기</div>
          <h1 className="mt-4 text-[32px] font-black tracking-tight text-slate-950">
            {isEditMode ? '게시글을 수정해보세요' : '공연을 본 사람의 시선으로 남겨보세요'}
          </h1>
          <p className="mt-2 text-sm text-slate-500">
            {isEditMode ? '제목, 카테고리, 내용을 다시 정리할 수 있어요.' : '후기, 꿀팁, 요청 글을 빠르게 작성할 수 있어요.'}
          </p>
        </div>

        <form onSubmit={submit} className="space-y-5">
          <label className="block">
            <div className="mb-2 text-sm font-medium text-slate-700">카테고리</div>
            <select
              value={form.categoryId}
              onChange={(e) => setForm((prev) => ({ ...prev, categoryId: e.target.value }))}
              className="w-full rounded-full border border-[var(--line)] bg-slate-50 px-4 py-3 text-sm outline-none"
            >
              <option value="">선택하세요</option>
              {categories.map((category: any) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </label>

          {isRequestCategory && (
            <>
              <div className="block">
                <div className="mb-2 text-sm font-medium text-slate-700">요청 유형</div>
                <div className="flex flex-wrap gap-2">
                  <VisiButton active={requestType === 'general'} onClick={() => setRequestType('general')}>일반요청</VisiButton>
                  <VisiButton active={requestType === 'event'} onClick={() => setRequestType('event')}>이벤트행사요청</VisiButton>
                </div>
              </div>

              <div className="block">
                <div className="mb-2 text-sm font-medium text-slate-700">공개 범위</div>
                <div className="flex flex-wrap gap-2">
                  <VisiButton active={visibility === 'private'} onClick={() => setVisibility('private')}>비공개</VisiButton>
                  <VisiButton active={visibility === 'public'} onClick={() => setVisibility('public')}>공개</VisiButton>
                </div>
                <p className="mt-2 text-xs text-slate-500">
                  요청 글만 공개 범위를 선택할 수 있어요. 일반 글은 공개로 저장됩니다.
                </p>
              </div>

              {requestType === 'event' && (
                <div className="space-y-3 rounded-[22px] border border-[var(--line)] bg-slate-50 p-4">
                  <div className="text-sm font-semibold text-slate-700">이벤트행사요청 폼</div>
                  <select
                    value={requestForm.categoryId}
                    onChange={(e) => setRequestForm((prev) => ({ ...prev, categoryId: e.target.value }))}
                    className="w-full rounded-full border border-[var(--line)] bg-white px-4 py-3 text-sm outline-none"
                  >
                    <option value="">행사 카테고리 선택</option>
                    {eventCategories.map((category: any) => (
                      <option key={category.id} value={category.id}>
                        {category.name}
                      </option>
                    ))}
                  </select>
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
                </div>
              )}
            </>
          )}

          <label className="block">
            <div className="mb-2 text-sm font-medium text-slate-700">제목</div>
            <input
              value={form.title}
              onChange={(e) => setForm((prev) => ({ ...prev, title: e.target.value }))}
              className="w-full rounded-full border border-[var(--line)] bg-slate-50 px-4 py-3 text-sm outline-none"
            />
          </label>

          <label className="block">
            <div className="mb-2 text-sm font-medium text-slate-700">내용</div>
            <textarea
              value={form.content}
              onChange={(e) => setForm((prev) => ({ ...prev, content: e.target.value }))}
              className="min-h-48 w-full rounded-[24px] border border-[var(--line)] bg-slate-50 p-4 text-sm outline-none"
            />
          </label>

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
      </div>
    </div>
  )
}

function VisiButton({ active, onClick, children }: { active: boolean; onClick: () => void; children: string }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`rounded-full px-4 py-2 text-xs font-semibold transition-colors ${
        active ? 'bg-[var(--accent-soft)] text-[var(--accent)]' : 'bg-slate-100 text-slate-500 hover:bg-slate-200'
      }`}
    >
      {children}
    </button>
  )
}
