import client from './client'
import { unwrap, unwrapPageResponse } from '../lib/api'
import type { FavoriteItem, FavoritePage } from '../types/favorite'

export const getFavorites = async (params: Record<string, any> = {}) => {
  const res = await client.get('/favorite-service/v1/favorites', { params })
  return unwrapPageResponse<FavoriteItem>(res.data) as FavoritePage
}

export const createFavorite = async (payload: { eventId: string; categoryId: string }) => {
  const res = await client.post('/favorite-service/v1/favorites', payload)
  return unwrap<{ eventId: string; eventName: string; userId: string }>(res.data)
}

export const deleteFavorite = async (favoriteId: string) => {
  const res = await client.delete(`/favorite-service/v1/favorites/${favoriteId}`)
  return unwrap<void>(res.data)
}
