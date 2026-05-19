import type { ReactNode } from 'react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter, Link, Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import Home from './pages/Home'
import Events from './pages/Events'
import EventDetail from './pages/EventDetail'
import Community from './pages/Community'
import PostDetail from './pages/PostDetail'
import CommunityWrite from './pages/CommunityWrite'
import Calendar from './pages/Calendar'
import Admin from './pages/Admin'
import Login from './pages/Login'
import Register from './pages/Register'
import MyPage from './pages/MyPage'
import Notices from './pages/Notices'
import NoticeDetail from './pages/NoticeDetail'
import NoticeWrite from './pages/NoticeWrite'
import { useAuthStore } from './store/authStore'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 1000 * 30,
    },
  },
})

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route path="/" element={<Home />} />
            <Route path="/events" element={<Events />} />
            <Route path="/events/:eventId" element={<EventDetail />} />
            <Route path="/calendar" element={<Navigate to="/" replace />} />
            <Route path="/community" element={<Community />} />
            <Route path="/community/new" element={<CommunityWrite />} />
            <Route path="/community/:postId/edit" element={<CommunityWrite />} />
            <Route path="/community/:postId" element={<PostDetail />} />
            <Route path="/notices" element={<NoticeReadGuard><Notices /></NoticeReadGuard>} />
            <Route path="/notices/new" element={<NoticeWrite />} />
            <Route path="/notices/:noticeId" element={<NoticeReadGuard><NoticeDetail /></NoticeReadGuard>} />
            <Route path="/notices/:noticeId/edit" element={<NoticeWrite />} />
            <Route path="/admin" element={<AdminRouteGuard />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/my" element={<MyPage />} />
            <Route path="/my/calendars" element={<Calendar mode="mine" />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}

function AdminRouteGuard() {
  const { user } = useAuthStore()
  if (!user) return <Navigate to="/login" replace />
  const normalizedRole = String(user.role ?? '').replace(/^ROLE_/, '')
  const canAccessAdmin =
    normalizedRole === 'ADMIN' || normalizedRole === 'MANAGER' || normalizedRole.endsWith('_MANAGER')
  if (!canAccessAdmin) return <Navigate to="/" replace />
  return <Admin />
}

function NoticeReadGuard({ children }: { children: ReactNode }) {
  const { user } = useAuthStore()

  if (!user) {
    return (
      <div className="px-5 py-10 md:px-8 md:py-12">
        <div className="mx-auto max-w-2xl rounded-[24px] border border-[var(--line)] bg-white p-6 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
          <div className="inline-flex rounded-full bg-[var(--accent-soft)] px-3 py-1 text-[11px] font-semibold text-[var(--accent)]">
            공지사항
          </div>
          <h1 className="mt-3 text-[24px] font-black tracking-tight text-slate-950">로그인이 필요해요</h1>
          <p className="mt-2 text-sm leading-6 text-slate-500">
            공지사항은 로그인한 사용자만 확인할 수 있어요. 로그인 후 다시 들어오면 바로 볼 수 있습니다.
          </p>
          <div className="mt-5 flex items-center gap-2">
            <Link
              to="/login"
              className="rounded-full bg-[var(--accent)] px-5 py-2.5 text-sm font-semibold text-white"
            >
              로그인하기
            </Link>
            <Link
              to="/"
              className="rounded-full border border-[var(--line)] bg-white px-5 py-2.5 text-sm font-semibold text-slate-700"
            >
              홈으로
            </Link>
          </div>
        </div>
      </div>
    )
  }

  return <>{children}</>
}
