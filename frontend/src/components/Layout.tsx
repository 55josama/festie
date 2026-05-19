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
    <div className="min-h-screen bg-[var(--page-bg)] px-0 py-0 lg:px-3 lg:py-3">
      <div className="mx-auto min-h-screen bg-white lg:min-h-[calc(100vh-1.5rem)] lg:min-w-[1280px] lg:w-full lg:rounded-[28px] lg:border lg:border-[var(--line)] lg:shadow-[0_20px_50px_rgba(15,23,42,0.06)]">
        <Header />
        <main className="bg-[var(--page-bg)] pb-20 lg:pb-0">
          <Outlet />
        </main>
        <ChatbotWidget />
      </div>
    </div>
  )
}
