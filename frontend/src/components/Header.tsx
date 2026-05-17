import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import { logout as logoutApi } from '../api/auth'
import NotificationBell from './NotificationBell'

const navClass = ({ isActive }: { isActive: boolean }) =>
  `border-b-2 px-1 py-2 text-[15px] font-medium transition-colors ${
    isActive
      ? 'border-[var(--accent)] text-[var(--accent)]'
      : 'border-transparent text-slate-600 hover:text-slate-900'
  }`

export default function Header() {
  const navigate = useNavigate()
  const { user, isLoggedIn, logout } = useAuthStore()
  const isManager = !!user && /ADMIN|MANAGER/.test(user.role)

  const handleLogout = async () => {
    try {
      await logoutApi()
    } finally {
      logout()
      navigate('/')
    }
  }

  return (
    <header className="sticky top-0 z-40 border-b border-[var(--line)] bg-white/92 backdrop-blur">
      <div className="flex flex-col gap-2 px-4 py-2.5">
        <div className="flex items-center justify-between gap-4 lg:hidden">
          <Link to="/" className="flex shrink-0 items-center whitespace-nowrap text-[22px] font-black leading-none tracking-tight text-slate-950">
            <span>Fest</span><span className="text-[var(--accent)]">ie</span>
          </Link>

          <div className="flex shrink-0 items-center gap-2">
            {user ? (
              <>
                <NotificationBell variant="mobile" />
                <Link
                  to="/my"
                  className="inline-flex items-center gap-2 rounded-full border border-[var(--line)] bg-white px-3 py-2 text-sm font-semibold text-slate-700 transition-colors hover:bg-slate-50"
                >
                  {user.nickname}
                </Link>
                <button
                  onClick={handleLogout}
                  className="rounded-full bg-[var(--accent)] px-4 py-2 text-sm font-semibold text-white"
                >
                  로그아웃
                </button>
              </>
            ) : (
              <Link to="/login" className="rounded-full border border-[var(--line)] bg-white px-4 py-2 text-sm font-semibold text-slate-700 transition-colors hover:bg-slate-50">
                로그인
              </Link>
            )}
          </div>
        </div>

        <nav className="flex min-w-0 items-center justify-start gap-4 overflow-x-auto whitespace-nowrap pb-0 pl-1 lg:hidden">
          <NavLink to="/" end className={navClass}>홈</NavLink>
          <NavLink to="/events" className={navClass}>행사</NavLink>
          <NavLink to="/calendar" className={navClass}>캘린더</NavLink>
          <NavLink to="/community" className={navClass}>커뮤니티</NavLink>
          <NavLink to="/my/calendars" className={navClass}>MY</NavLink>
          {isManager && <NavLink to="/admin" className={navClass}>관리</NavLink>}
        </nav>

        <div className="relative hidden w-full items-center justify-between gap-6 lg:flex">
          <Link to="/" className="flex shrink-0 items-center whitespace-nowrap text-[22px] font-black leading-none tracking-tight text-slate-950">
            <span>Fest</span><span className="text-[var(--accent)]">ie</span>
          </Link>

          <nav className="absolute left-1/2 top-1/2 flex min-w-0 -translate-x-1/2 -translate-y-1/2 items-center justify-center gap-5 whitespace-nowrap">
            <NavLink to="/" end className={navClass}>홈</NavLink>
            <NavLink to="/events" className={navClass}>행사</NavLink>
            <NavLink to="/calendar" className={navClass}>캘린더</NavLink>
            <NavLink to="/community" className={navClass}>커뮤니티</NavLink>
            <NavLink to="/my/calendars" className={navClass}>MY</NavLink>
            {isManager && <NavLink to="/admin" className={navClass}>관리</NavLink>}
          </nav>

          <div className="flex shrink-0 items-center gap-3">
            {user ? (
              <Link
                to="/my"
                className="inline-flex items-center gap-2 rounded-full border border-[var(--line)] bg-white px-3 py-2 text-sm font-semibold text-slate-700 transition-colors hover:bg-slate-50"
              >
                <span className="inline-flex h-6 w-6 items-center justify-center rounded-full bg-[var(--accent-soft)] text-[var(--accent)]">
                  {user.nickname?.[0] ?? 'U'}
                </span>
                {user.nickname}
              </Link>
            ) : (
              <Link to="/login" className="rounded-full border border-[var(--line)] px-4 py-2 text-sm font-medium text-slate-700">
                로그인
              </Link>
            )}
            {user && <NotificationBell variant="desktop" />}
            {isLoggedIn() ? (
              <button
                onClick={handleLogout}
                className="rounded-full bg-[var(--accent)] px-4 py-2 text-sm font-semibold text-white"
              >
                로그아웃
              </button>
            ) : (
              <Link to="/register" className="rounded-full border border-[var(--line)] bg-[var(--accent-soft)] px-4 py-2 text-sm font-semibold text-[var(--accent)]">
                회원가입
              </Link>
            )}
          </div>
        </div>
      </div>
    </header>
  )
}
