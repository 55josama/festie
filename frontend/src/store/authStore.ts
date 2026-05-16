import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { buildUserFromToken } from '../lib/jwt'
import type { User } from '../types'

interface AuthState {
  accessToken: string | null
  refreshToken: string | null
  user: User | null
  setAccessToken: (token: string) => void
  setRefreshToken: (token: string) => void
  setUser: (user: User) => void
  syncUserFromAccessToken: () => void
  logout: () => void
  isLoggedIn: () => boolean
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      refreshToken: null,
      user: null,
      setAccessToken: (token) => set({ accessToken: token }),
      setRefreshToken: (token) => set({ refreshToken: token }),
      setUser: (user) => set({ user }),
      syncUserFromAccessToken: () => {
        const state = get()
        if (state.user || !state.accessToken) return
        const fallbackUser = buildUserFromToken(state.accessToken)
        if (fallbackUser) {
          set({ user: fallbackUser })
        }
      },
      logout: () => set({ accessToken: null, refreshToken: null, user: null }),
      isLoggedIn: () => !!get().accessToken,
    }),
    { name: 'festie-auth', partialize: (s) => ({ accessToken: s.accessToken, refreshToken: s.refreshToken, user: s.user }) }
  )
)
