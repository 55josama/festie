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
    <div className="min-h-screen bg-[var(--page-bg)] px-0 py-0 sm:px-3 sm:py-3">
      <div className="mx-auto min-h-screen max-w-[1280px] overflow-x-hidden bg-white sm:min-h-[calc(100vh-1.5rem)] sm:rounded-[28px] sm:border sm:border-[var(--line)] sm:shadow-[0_20px_50px_rgba(15,23,42,0.06)]">
        <Header />
        <main className="bg-[var(--page-bg)] pb-20 sm:pb-0">
          <Outlet />
        </main>
        <ChatbotWidget />
      </div>
    </div>
  )
}
