import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { createPost, getCategories } from '../api/community'

export default function CommunityWrite() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const { data: categories = [] } = useQuery({ queryKey: ['categories'], queryFn: getCategories })
  const [form, setForm] = useState({ title: '', content: '', categoryId: '' })
  const [visibility, setVisibility] = useState<'public' | 'private'>('public')
  const [requestType, setRequestType] = useState<'general' | 'event'>('general')
  const [requestForm, setRequestForm] = useState({
    eventName: '',
    eventDate: '',
    eventPlace: '',
    eventLink: '',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const selectedCategory = useMemo(() => categories.find((category: any) => category.id === form.categoryId), [categories, form.categoryId])
  const isRequestCategory = selectedCategory?.name === '요청'

  useEffect(() => {
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
  }, [categories, searchParams])

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

      const result = await createPost({
        ...form,
        title: isRequestCategory && requestType === 'event' ? `[행사 요청] ${form.title}` : form.title,
        content: isRequestCategory ? requestDescription : form.content,
        visibility: isRequestCategory && visibility === 'private' ? 'PRIVATE' : 'PUBLIC',
      })
      navigate(`/community/${result.id}`)
    } catch (err: any) {
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
          <h1 className="mt-4 text-[32px] font-black tracking-tight text-slate-950">공연을 본 사람의 시선으로 남겨보세요</h1>
          <p className="mt-2 text-sm text-slate-500">후기, 꿀팁, 요청 글을 빠르게 작성할 수 있어요.</p>
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
                  <input
                    value={requestForm.eventName}
                    onChange={(e) => setRequestForm((prev) => ({ ...prev, eventName: e.target.value }))}
                    placeholder="행사명"
                    className="w-full rounded-full border border-[var(--line)] bg-white px-4 py-3 text-sm outline-none"
                  />
                  <input
                    value={requestForm.eventDate}
                    onChange={(e) => setRequestForm((prev) => ({ ...prev, eventDate: e.target.value }))}
                    placeholder="예정 날짜"
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
            <Link to="/community" className="rounded-full bg-slate-100 px-5 py-3 text-sm font-semibold text-slate-600 hover:bg-slate-200">
              취소
            </Link>
            <button disabled={loading} className="rounded-full bg-[var(--accent-soft)] px-5 py-3 text-sm font-semibold text-[var(--accent)] transition-colors hover:bg-slate-100">
              {loading ? '저장 중...' : '등록하기'}
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
