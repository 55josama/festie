export function unwrap<T>(payload: any): T {
  if (payload && typeof payload === 'object' && 'data' in payload) {
    return (payload.data as T)
  }
  return payload as T
}

export function unwrapPage<T>(payload: any): T[] {
  const data = unwrap<any>(payload)
  if (Array.isArray(data)) return data
  return data?.content ?? []
}
