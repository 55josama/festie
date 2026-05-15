import client from './client'
import { unwrap } from '../lib/api'

export const createOperationRequest = async (payload: {
  title: string
  content: string
}) => {
  const res = await client.post('/operation-service/v1/operation-requests', payload)
  return unwrap<{ id: string }>(res.data)
}
