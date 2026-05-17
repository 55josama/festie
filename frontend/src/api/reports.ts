import client from './client'
import { unwrap } from '../lib/api'

export const createReport = async (data: {
  targetType: string
  targetId: string
  category?: string
  description: string
  content?: string
}) => {
  const res = await client.post('/operation-service/v1/reports', data)
  return unwrap(res.data)
}
