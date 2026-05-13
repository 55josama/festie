export interface EventRequestItem {
  id: string
  requesterId: string
  eventName: string
  categoryId: string
  category: string
  link: string
  description: string
  rejectReason: string | null
  status: string
  createdEventId?: string | null
}

export interface ReportItem {
  id: string
  reporterId: string
  reporterType: string
  targetId: string
  targetType: string
  category: string
  description: string
  targetContent?: string | null
  status: string
  operatorMemo: string | null
}
