import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { createReport } from '../api/reports'
import { useAuthStore } from '../store/authStore'

type ReportButtonProps = {
  targetType: string
  targetId: string
  targetContent: string
  label?: string
  className?: string
}

const REPORT_CATEGORY_OPTIONS = [
  { value: 'PROFANITY', label: '욕설 / 비방' },
  { value: 'HATE_SPEECH', label: '혐오 표현' },
  { value: 'SEXUAL_CONTENT', label: '음란 / 성적 표현' },
  { value: 'SPAM', label: '도배 / 스팸' },
  { value: 'SCAM', label: '사기 / 허위 정보' },
  { value: 'PRIVACY_LEAK', label: '개인정보 노출' },
  { value: 'UNAUTHORIZED_TRADE', label: '불법 거래 유도' },
  { value: 'OTHER', label: '기타' },
] as const

export default function ReportButton({ targetType, targetId, targetContent, label = '신고', className = 'rounded-full border border-[var(--line)] bg-white px-4 py-2 text-sm font-medium text-slate-700' }: ReportButtonProps) {
  const { isLoggedIn } = useAuthStore()
  const [open, setOpen] = useState(false)
  const [category, setCategory] = useState<(typeof REPORT_CATEGORY_OPTIONS)[number]['value']>('OTHER')
  const [description, setDescription] = useState('')

  const mutation = useMutation({
    mutationFn: (payload: { category: string; description: string }) => createReport({
      targetType,
      targetId,
      category: payload.category,
      description: payload.description,
      content: targetContent,
    }),
    onSuccess: () => {
      window.alert('신고가 접수되었습니다.')
      setOpen(false)
      setDescription('')
      setCategory('OTHER')
    },
  })

  if (!isLoggedIn()) return null

  return (
    <>
      <button
        type="button"
        onClick={() => setOpen(true)}
        disabled={mutation.isPending}
        className={className}
      >
        {mutation.isPending ? '접수 중...' : label}
      </button>

      {open && (
        <div className="fixed inset-0 z-[80] flex items-center justify-center px-4">
          <button
            type="button"
            aria-label="신고 창 닫기"
            className="absolute inset-0 bg-black/25"
            onClick={() => setOpen(false)}
          />

          <div className="relative w-full max-w-[420px] rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_24px_60px_rgba(15,23,42,0.22)]">
            <div className="text-[18px] font-black tracking-tight text-slate-950">신고하기</div>
            <div className="mt-1 text-sm text-slate-500">신고 유형을 선택하고 자세한 사유를 적어주세요.</div>

            <div className="mt-4 grid grid-cols-2 gap-2">
              {REPORT_CATEGORY_OPTIONS.map((option) => (
                <label
                  key={option.value}
                  className={`flex min-h-[52px] cursor-pointer items-center gap-3 rounded-[16px] border px-3 py-3 text-sm transition-colors ${
                    category === option.value
                      ? 'border-[var(--accent-soft)] bg-[var(--accent-soft)]/20 text-slate-950'
                      : 'border-[var(--line)] bg-slate-50 text-slate-700 hover:bg-white'
                  }`}
                >
                  <input
                    type="radio"
                    name="report-category"
                    value={option.value}
                    checked={category === option.value}
                    onChange={() => setCategory(option.value)}
                    className="h-4 w-4 accent-[var(--accent)]"
                  />
                  <span className="font-medium">{option.label}</span>
                </label>
              ))}
            </div>

            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="신고 사유를 자세히 적어주세요."
              className="mt-4 min-h-[120px] w-full rounded-[18px] border border-[var(--line)] bg-slate-50 px-4 py-3 text-[16px] font-medium leading-7 text-slate-900 outline-none placeholder:text-slate-400 focus:border-[var(--accent)]"
            />

            <div className="mt-4 flex items-center justify-end gap-2">
              <button
                type="button"
                onClick={() => setOpen(false)}
                className="rounded-full border border-[var(--line)] bg-white px-4 py-2 text-sm font-semibold text-slate-700"
              >
                취소
              </button>
              <button
                type="button"
                onClick={() => {
                  const nextDescription = description.trim()
                  if (!nextDescription) {
                    window.alert('신고 사유를 입력해주세요.')
                    return
                  }
                  mutation.mutate({ category, description: nextDescription })
                }}
                disabled={mutation.isPending}
                className="rounded-full bg-[var(--accent)] px-4 py-2 text-sm font-semibold text-white disabled:opacity-70"
              >
                신고 접수
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
