import type { User } from '../types'

type JwtPayload = {
  sub?: string
  email?: string
  role?: string
  nickname?: string
  tokenType?: string
}

function base64UrlToBase64(value: string) {
  return value.replace(/-/g, '+').replace(/_/g, '/').padEnd(Math.ceil(value.length / 4) * 4, '=')
}

function decodeBase64Json<T>(value: string): T | null {
  try {
    const binary = atob(base64UrlToBase64(value))
    const bytes = Uint8Array.from(binary, (char) => char.charCodeAt(0))
    const text = new TextDecoder().decode(bytes)
    return JSON.parse(text) as T
  } catch {
    return null
  }
}

export function decodeJwtPayload(token: string) {
  const parts = token.split('.')
  if (parts.length < 2) return null
  return decodeBase64Json<JwtPayload>(parts[1])
}

export function buildUserFromToken(token: string): User | null {
  const payload = decodeJwtPayload(token)
  if (!payload?.sub || !payload.email) return null

  const nickname = payload.nickname ?? payload.email.split('@')[0] ?? '사용자'

  return {
    userId: payload.sub,
    email: payload.email,
    nickname,
    name: nickname,
    phoneNumber: '',
    role: payload.role ?? 'USER',
  }
}
