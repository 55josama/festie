import { Outlet } from 'react-router-dom'
import Header from './Header'

export default function Layout() {
  return (
    <div className="min-h-screen px-3 py-3 sm:px-4 sm:py-4">
      <div className="mx-auto min-h-[calc(100vh-1.5rem)] max-w-[1280px] overflow-hidden rounded-[28px] border border-[var(--line)] bg-white shadow-[0_20px_50px_rgba(15,23,42,0.06)]">
        <Header />
        <main className="bg-[var(--page-bg)]">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
