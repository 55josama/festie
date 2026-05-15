export interface EventRequestItem {
  id: string
  requesterId: string
  requesterNickname?: string
  createdAt?: string
  eventName: string
  categoryId: string
  category: string
  link: string
  description: string
  rejectReason: string | null
  status: string
  createdEventId?: string | null
}

export interface OperationRequestItem {
  id: string
  requesterId: string
  requesterNickname?: string
  createdAt?: string
  title: string
  content: string
  status: string
  adminMemo: string | null
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

export interface AdminMessageItem {
  messageId: string
  chatRoomId: string
  userId: string
  writerNickname: string
  messageType: string
  content: string
  status: string
  createdAt: string
}

export type AdminUserRole =
  | 'USER'
  | 'ADMIN'
  | 'CONCERT_MANAGER'
  | 'FESTIVAL_MANAGER'
  | 'FANMEETING_MANAGER'
  | 'POPUP_MANAGER'
  | 'COMMUNITY_MANAGER'

export interface AdminUserItem {
  userId: string
  email: string
  nickname: string
  name: string
  role: AdminUserRole
  createdAt: string
  updatedAt: string
}

export interface AdminUserPage {
  content: AdminUserItem[]
  page: number
  totalElements: number
  totalPages: number
  size: number
}
