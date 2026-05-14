export function formatDateTime(value?: string | null) {
  if (!value) return '정보 없음'
  return new Date(value).toLocaleString('ko-KR', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function formatDateRange(startAt?: string | null, endAt?: string | null) {
  if (!startAt) return '일정 미정'
  const start = new Date(startAt)
  const end = endAt ? new Date(endAt) : null

  const startText = start.toLocaleDateString('ko-KR', { month: '2-digit', day: '2-digit' })
  const startTime = start.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
  if (!end) return `${startText} ${startTime}`

  const endText = end.toLocaleDateString('ko-KR', { month: '2-digit', day: '2-digit' })
  const endTime = end.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
  return `${startText} ${startTime} - ${endText} ${endTime}`
}

export function formatPrice(minFee?: number | null, maxFee?: number | null) {
  if (!minFee && !maxFee) return '무료'
  const format = (value: number) => new Intl.NumberFormat('ko-KR').format(value)
  if (!maxFee || minFee === maxFee) return `${format(minFee ?? maxFee ?? 0)}원`
  return `${format(minFee ?? 0)}원 ~ ${format(maxFee)}원`
}

export function formatRelativeTime(value?: string | null) {
  if (!value) return '방금 전'
  const diff = Date.now() - new Date(value).getTime()
  const minutes = Math.max(0, Math.floor(diff / 60000))
  if (minutes < 1) return '방금 전'
  if (minutes < 60) return `${minutes}분 전`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}시간 전`
  const days = Math.floor(hours / 24)
  return `${days}일 전`
}

export function getDDay(dateValue?: string | null) {
  if (!dateValue) return null
  const target = new Date(dateValue)
  const today = new Date()
  const start = new Date(today.getFullYear(), today.getMonth(), today.getDate())
  const diff = Math.ceil((target.getTime() - start.getTime()) / 86400000)
  if (diff > 0) return `D-${diff}`
  if (diff === 0) return 'D-Day'
  return `D+${Math.abs(diff)}`
}

export function formatMonthHeader(year: number, month: number) {
  return `${year}년 ${month}월`
}

export function getMonthKey(year: number, month: number, day: number) {
  return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
}
