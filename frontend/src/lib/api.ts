export function unwrap<T>(payload: any): T {
  if (payload && typeof payload === 'object' && 'data' in payload) {
    return (payload.data as T)
  }
  return payload as T
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export function unwrapPageResponse<T>(payload: any): PageResponse<T> {
  const data = unwrap<any>(payload)
  if (Array.isArray(data)) {
    return {
      content: data as T[],
      page: 0,
      size: data.length,
      totalElements: data.length,
      totalPages: 1,
    }
  }
  return {
    content: data?.content ?? [],
    page: data?.page ?? 0,
    size: data?.size ?? data?.content?.length ?? 0,
    totalElements: data?.totalElements ?? data?.content?.length ?? 0,
    totalPages: data?.totalPages ?? 1,
  }
}

export function unwrapPage<T>(payload: any): T[] {
  return unwrapPageResponse<T>(payload).content
}
