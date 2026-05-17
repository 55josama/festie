import client from './client'
import { unwrap, unwrapPage, unwrapPageResponse } from '../lib/api'
import type { ChatRoom, Event } from '../types'
import type { AdminUserDetailItem, AdminUserItem, AdminUserPage, BlacklistItem, BlacklistPage, EventRequestItem, OperationRequestItem, ReportItem, ReportPage } from '../types/admin'

export const getEventRequests = async (params: Record<string, any> = {}) => {
  const res = await client.get('/event-service/v1/event-requests', { params })
  return unwrapPage<EventRequestItem>(res.data)
}

export const approveEventRequest = async (requestId: string) => {
  const res = await client.post(`/event-service/v1/event-requests/${requestId}/approval`)
  return unwrap<EventRequestItem>(res.data)
}

export const rejectEventRequest = async (requestId: string, rejectReason: string) => {
  const res = await client.post(`/event-service/v1/event-requests/${requestId}/rejections`, { rejectReason })
  return unwrap<EventRequestItem>(res.data)
}

export const getEventRequest = async (requestId: string) => {
  const res = await client.get(`/event-service/v1/event-requests/${requestId}`)
  return unwrap<EventRequestItem>(res.data)
}

export const deleteEventRequest = async (requestId: string) => {
  const res = await client.delete(`/event-service/v1/event-requests/admin/${requestId}`)
  return unwrap<void>(res.data)
}

export const getOperationRequests = async (params: Record<string, any> = {}) => {
  const res = await client.get('/operation-service/v1/operation-requests', { params })
  return unwrapPage<OperationRequestItem>(res.data)
}

export const updateOperationRequestStatus = async (requestId: string, status: string, adminMemo: string) => {
  const res = await client.patch(`/operation-service/v1/operation-requests/${requestId}/status`, { status, adminMemo })
  return unwrap<OperationRequestItem>(res.data)
}

export const getReports = async (params: Record<string, any> = {}) => {
  const res = await client.get('/operation-service/v1/reports', { params })
  return unwrapPageResponse<ReportItem>(res.data) as ReportPage
}

export const updateReportStatus = async (reportId: string, status: string, operatorMemo: string) => {
  const res = await client.patch(`/operation-service/v1/reports/${reportId}/status`, { status, operatorMemo })
  return unwrap<ReportItem>(res.data)
}

export const getPopularChatRooms = async (limit = 6) => {
  const res = await client.get('/chat-service/v1/chat/rooms/popular', { params: { limit } })
  return unwrapPage<ChatRoom & { currentViewerCount?: number }>(res.data)
}

export const getAdminChatRooms = async () => {
  const res = await client.get('/chat-service/v1/chat/admin/rooms')
  return unwrapPage<ChatRoom & { currentViewerCount?: number }>(res.data)
}

export const forceChatRoomStatus = async (chatRoomId: string, action: 'FORCE_OPEN' | 'FORCE_CLOSE') => {
  const res = await client.patch(`/chat-service/v1/chat/admin/rooms/${chatRoomId}/status`, { action })
  return unwrap<ChatRoom>(res.data)
}

export const getAdminEvents = async (params: Record<string, any> = {}) => {
  const res = await client.get('/event-service/v1/events', { params })
  return unwrapPage<Event>(res.data)
}

export const getAdminUsers = async (params: Record<string, any> = {}) => {
  const res = await client.get('/user-service/v1/users/admin', { params })
  return unwrapPageResponse<AdminUserItem>(res.data) as AdminUserPage
}

export const getAdminUserDetail = async (userId: string) => {
  const res = await client.get(`/user-service/v1/users/admin/${userId}`)
  return unwrap<AdminUserDetailItem>(res.data)
}

export const changeAdminUserRole = async (userId: string, role: string) => {
  const res = await client.patch(`/user-service/v1/users/admin/${userId}/role`, { role })
  return unwrap<AdminUserItem>(res.data)
}

export const getBlacklists = async (params: Record<string, any> = {}) => {
  const res = await client.get('/operation-service/v1/blacklists', { params })
  return unwrapPageResponse<BlacklistItem>(res.data) as BlacklistPage
}

export const createBlacklist = async (userId: string, reason: string) => {
  const res = await client.post('/operation-service/v1/blacklists', { userId, reason })
  return unwrap<BlacklistItem>(res.data)
}

export const releaseBlacklist = async (blacklistId: string, reason: string) => {
  const res = await client.patch(`/operation-service/v1/blacklists/${blacklistId}/status`, { reason })
  return unwrap<BlacklistItem>(res.data)
}
