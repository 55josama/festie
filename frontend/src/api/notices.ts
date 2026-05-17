import client from './client'
import { unwrap, unwrapPageResponse } from '../lib/api'

export type NoticeItem = {
  noticeId: string
  adminId: string
  title: string
  content: string
}

export const getNotices = async (params: Record<string, any> = {}) => {
  const res = await client.get('/operation-service/v1/notices', { params })
  return unwrapPageResponse<NoticeItem>(res.data)
}

export const getNotice = async (noticeId: string) => {
  const res = await client.get(`/operation-service/v1/notices/${noticeId}`)
  return unwrap<NoticeItem>(res.data)
}

export const createNotice = async (title: string, content: string) => {
  const res = await client.post('/operation-service/v1/notices', { title, content })
  return unwrap<NoticeItem>(res.data)
}

export const updateNotice = async (noticeId: string, title: string, content: string) => {
  const res = await client.patch(`/operation-service/v1/notices/${noticeId}`, { title, content })
  return unwrap<NoticeItem>(res.data)
}

export const deleteNotice = async (noticeId: string) => {
  const res = await client.delete(`/operation-service/v1/notices/${noticeId}`)
  return unwrap<void>(res.data)
}
