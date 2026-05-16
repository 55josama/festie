import { Outlet } from 'react-router-dom'
import { useEffect } from 'react'
import { useAuthStore } from '../store/authStore'
import Header from './Header'
import ChatbotWidget from './ChatbotWidget'

export default function Layout() {
  const syncUserFromAccessToken = useAuthStore((state) => state.syncUserFromAccessToken)

  useEffect(() => {
    syncUserFromAccessToken()
  }, [syncUserFromAccessToken])

  return (
    <div className="min-h-screen px-3 py-3 sm:px-4 sm:py-4">
      <div className="mx-auto min-h-[calc(100vh-1.5rem)] max-w-[1280px] overflow-hidden rounded-[28px] border border-[var(--line)] bg-white shadow-[0_20px_50px_rgba(15,23,42,0.06)]">
        <Header />
        <main className="bg-[var(--page-bg)]">
          <Outlet />
        </main>
        <ChatbotWidget />
      </div>
    </div>
  )
}
