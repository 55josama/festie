export interface Event {
  id: string
  name: string
  categoryId: string
  categoryName: string
  startAt: string
  endAt: string
  place: string
  region?: string
  latitude?: number
  longitude?: number
  radius?: number
  minFee: number | null
  maxFee: number | null
  hasTicketing: boolean
  ticketingOpenAt?: string | null
  ticketingCloseAt?: string | null
  ticketingLink?: string | null
  officialLink?: string | null
  description: string | null
  performer?: string | null
  img: string | null
  status: EventStatus
  schedules?: EventSchedule[]
}

export type EventStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'

export const STATUS_LABEL: Record<EventStatus, string> = {
  SCHEDULED: '예정',
  IN_PROGRESS: '진행중',
  COMPLETED: '종료',
  CANCELLED: '취소',
}

export const STATUS_STYLE: Record<EventStatus, string> = {
  SCHEDULED: 'bg-blue-50 text-blue-600',
  IN_PROGRESS: 'bg-emerald-50 text-emerald-600',
  COMPLETED: 'bg-gray-100 text-gray-400',
  CANCELLED: 'bg-red-50 text-red-400',
}

export interface Post {
  id: string
  userId: string
  categoryId: string
  categoryName: string
  visibility?: 'PUBLIC' | 'PRIVATE'
  title: string
  content: string
  viewCount: number
  likeCount: number
  commentCount: number
  status: string
  createdAt: string
  updatedAt?: string
  authorNickname?: string
  eventName?: string
}

export interface Category {
  id: string
  name: string
}

export interface ChatMessage {
  messageId: string
  chatRoomId: string
  userId: string
  writerNickname: string
  messageType: 'USER' | 'SYSTEM'
  content: string
  status: string
  createdAt: string
}

export interface ChatRoom {
  chatRoomId: string
  eventId: string
  eventName: string
  category: string
  status: string
  scheduledOpenAt: string | null
  scheduledCloseAt: string | null
  openedAt: string | null
  closedAt: string | null
  currentViewerCount?: number
}

export interface Comment {
  id: string
  postId: string
  userId: string
  parentId: string | null
  content: string
  status: string
  likeCount: number
  createdAt: string
  updatedAt: string
  replies?: Comment[]
}

export interface CalendarEntry {
  id: string
  userId?: string
  eventDate: string
  ticketingDate: string
  memo: string
  eventName: string
  eventId: string
  eventStatus: string
}

export interface Notification {
  id: string
  title: string
  content: string
  readAt: string | null
}

export interface User {
  userId: string
  email: string
  nickname: string
  name: string
  role: string
}

export interface EventSchedule {
  id?: string
  title?: string
  startAt: string
  endAt: string
  venue?: string
  memo?: string
}
