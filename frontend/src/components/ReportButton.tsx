import { useMutation } from '@tanstack/react-query'
import { useAuthStore } from '../store/authStore'
import { createReport } from '../api/reports'

type ReportButtonProps = {
  targetType: string
  targetId: string
  label?: string
  className?: string
}

export default function ReportButton({ targetType, targetId, label = '신고', className = 'rounded-full border border-[var(--line)] bg-white px-4 py-2 text-sm font-medium text-slate-700' }: ReportButtonProps) {
  const { isLoggedIn } = useAuthStore()

  const mutation = useMutation({
    mutationFn: (description: string) => createReport({
      targetType,
      targetId,
      category: 'OTHER',
      description,
    }),
    onSuccess: () => {
      window.alert('신고가 접수되었습니다.')
    },
  })

  if (!isLoggedIn()) return null

  return (
    <button
      type="button"
      onClick={() => {
        const reason = window.prompt('신고 사유를 입력해주세요.')
        if (!reason?.trim()) return
        mutation.mutate(reason.trim())
      }}
      disabled={mutation.isPending}
      className={className}
    >
      {mutation.isPending ? '접수 중...' : label}
    </button>
  )
}
