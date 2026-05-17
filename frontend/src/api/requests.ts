import client from './client'
import { unwrap, unwrapPage } from '../lib/api'
import type { EventRequestItem, OperationRequestItem } from '../types/admin'

export const createOperationRequest = async (payload: {
  title: string
  content: string
}) => {
  const res = await client.post('/operation-service/v1/operation-requests', payload)
  return unwrap<{ id: string }>(res.data)
}

export const getOperationRequest = async (requestId: string) => {
  const res = await client.get(`/operation-service/v1/operation-requests/${requestId}`)
  return unwrap<OperationRequestItem>(res.data)
}

export const updateOperationRequest = async (requestId: string, payload: {
  title: string
  content: string
}) => {
  const res = await client.patch(`/operation-service/v1/operation-requests/${requestId}`, payload)
  return unwrap<OperationRequestItem>(res.data)
}

export const deleteOperationRequest = async (requestId: string) => {
  const res = await client.delete(`/operation-service/v1/operation-requests/${requestId}`)
  return unwrap<void>(res.data)
}

export const getEventRequests = async (params: Record<string, any> = {}) => {
  const res = await client.get('/event-service/v1/event-requests', { params })
  return unwrapPage<EventRequestItem>(res.data)
}

export const getMyEventRequests = async (params: Record<string, any> = {}) => {
  const res = await client.get('/event-service/v1/event-requests/me', { params })
  return unwrapPage<EventRequestItem>(res.data)
}

export const getMyEventRequest = async (requestId: string) => {
  const res = await client.get(`/event-service/v1/event-requests/me/${requestId}`)
  return unwrap<EventRequestItem>(res.data)
}

export const deleteEventRequest = async (requestId: string) => {
  const res = await client.delete(`/event-service/v1/event-requests/${requestId}`)
  return unwrap<void>(res.data)
}

export const getOperationRequests = async (params: Record<string, any> = {}) => {
  const res = await client.get('/operation-service/v1/operation-requests', { params })
  return unwrapPage<OperationRequestItem>(res.data)
}

export const getMyOperationRequests = async (params: Record<string, any> = {}) => {
  const res = await client.get('/operation-service/v1/operation-requests/me', { params })
  return unwrapPage<OperationRequestItem>(res.data)
}

export const getMyOperationRequest = async (requestId: string) => {
  const res = await client.get(`/operation-service/v1/operation-requests/me/${requestId}`)
  return unwrap<OperationRequestItem>(res.data)
}
