export interface FavoriteItem {
  id: string
  favoriteId: string
  eventId: string
  categoryId: string
  userId: string
  eventName: string
  eventImg: string | null
}

export interface FavoritePage {
  content: FavoriteItem[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}
