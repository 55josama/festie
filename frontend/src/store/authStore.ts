import { create } from 'zustand'
import { buildUserFromToken } from '../lib/jwt'
import type { User } from '../types'

const AUTH_STORAGE_KEY = 'festie-auth'

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

type StoredAuthState = Pick<AuthState, 'accessToken' | 'refreshToken' | 'user'>

function loadStoredAuthState(): StoredAuthState | null {
  if (typeof window === 'undefined') return null
  try {
    const raw = window.sessionStorage.getItem(AUTH_STORAGE_KEY)
    if (!raw) return null
    const parsed = JSON.parse(raw) as StoredAuthState
    return {
      accessToken: parsed.accessToken ?? null,
      refreshToken: parsed.refreshToken ?? null,
      user: parsed.user ?? null,
    }
  } catch {
    return null
  }
}

function saveStoredAuthState(state: StoredAuthState) {
  if (typeof window === 'undefined') return
  if (!state.accessToken && !state.refreshToken && !state.user) {
    window.sessionStorage.removeItem(AUTH_STORAGE_KEY)
    return
  }
  window.sessionStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(state))
}

const storedAuthState = loadStoredAuthState()

export const useAuthStore = create<AuthState>()(
  (set, get) => ({
    accessToken: storedAuthState?.accessToken ?? null,
    refreshToken: storedAuthState?.refreshToken ?? null,
    user: storedAuthState?.user ?? null,
    setAccessToken: (token) => {
      set({ accessToken: token })
      saveStoredAuthState({
        accessToken: token,
        refreshToken: get().refreshToken,
        user: get().user,
      })
    },
    setRefreshToken: (token) => {
      set({ refreshToken: token })
      saveStoredAuthState({
        accessToken: get().accessToken,
        refreshToken: token,
        user: get().user,
      })
    },
    setUser: (user) => {
      set({ user })
      saveStoredAuthState({
        accessToken: get().accessToken,
        refreshToken: get().refreshToken,
        user,
      })
    },
    syncUserFromAccessToken: () => {
      const state = get()
      if (state.user || !state.accessToken) return
      const fallbackUser = buildUserFromToken(state.accessToken)
      if (fallbackUser) {
        set({ user: fallbackUser })
        saveStoredAuthState({
          accessToken: state.accessToken,
          refreshToken: state.refreshToken,
          user: fallbackUser,
        })
      }
    },
    logout: () => {
      set({ accessToken: null, refreshToken: null, user: null })
      saveStoredAuthState({ accessToken: null, refreshToken: null, user: null })
    },
    isLoggedIn: () => !!get().accessToken,
  })
)
