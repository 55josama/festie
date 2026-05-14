import client from './client'
import { unwrap, unwrapPage } from '../lib/api'
import type { Category } from '../types'

export const getEventCategories = async () => {
  const res = await client.get('/event-service/v1/event-categories')
  return unwrapPage<Category>(res.data)
}

export const createEventCategory = async (name: string) => {
  const res = await client.post('/event-service/v1/event-categories', { name })
  return unwrap<Category>(res.data)
}

export const updateEventCategory = async (categoryId: string, name: string) => {
  const res = await client.patch(`/event-service/v1/event-categories/${categoryId}`, { name })
  return unwrap<Category>(res.data)
}

export const deleteEventCategory = async (categoryId: string) => {
  const res = await client.delete(`/event-service/v1/event-categories/${categoryId}`)
  return unwrap<void>(res.data)
}

export const getCommunityCategories = async () => {
  const res = await client.get('/community-service/v1/community-categories')
  return unwrapPage<Category>(res.data)
}

export const createCommunityCategory = async (name: string) => {
  const res = await client.post('/community-service/v1/community-categories', { name })
  return unwrap<Category>(res.data)
}

export const updateCommunityCategory = async (categoryId: string, name: string) => {
  const res = await client.patch(`/community-service/v1/community-categories/${categoryId}`, { name })
  return unwrap<Category>(res.data)
}

export const deleteCommunityCategory = async (categoryId: string) => {
  const res = await client.delete(`/community-service/v1/community-categories/${categoryId}`)
  return unwrap<void>(res.data)
}
