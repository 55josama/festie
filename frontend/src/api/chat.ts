import client from './client'
import { unwrap, unwrapPage } from '../lib/api'
import type { ChatMessage, ChatRoom } from '../types'

export const getChatRoomByEventId = async (eventId: string) => {
  const res = await client.get('/chat-service/v1/chat/rooms/event', { params: { eventId } })
  return unwrap<ChatRoom>(res.data)
}

export const getChatMessages = async (chatRoomId: string) => {
  const res = await client.get(`/chat-service/v1/chat/rooms/${chatRoomId}/messages`, { params: { page: 0, size: 30 } })
  const data = unwrap<any>(res.data)
  return unwrapPage<ChatMessage>({ data: data.messages ?? data.content ?? [] })
}

export const sendChatMessage = async (chatRoomId: string, content: string) => {
  const res = await client.post(`/chat-service/v1/chat/rooms/${chatRoomId}/messages`, { content })
  return unwrap<ChatMessage>(res.data)
}

export const getPopularChatRooms = async (limit = 3) => {
  const res = await client.get('/chat-service/v1/chat/rooms/popular', { params: { limit } })
  return unwrapPage<ChatRoom>(res.data)
}
