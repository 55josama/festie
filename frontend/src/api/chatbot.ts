import client from './client'
import { unwrap } from '../lib/api'

export const askChatbot = async (question: string) => {
  const res = await client.post('/ai-service/v1/chatbot', { question })
  const data = unwrap<{ answer: string }>(res.data)
  return data.answer
}
