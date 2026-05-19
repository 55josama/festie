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

const mobileNavClass = ({ isActive }: { isActive: boolean }) =>
  `flex flex-1 flex-col items-center justify-center gap-1 rounded-[16px] px-1.5 py-2 text-[10px] font-semibold transition-colors ${
    isActive
      ? 'bg-[var(--accent-soft)] text-[var(--accent)]'
      : 'text-slate-500 hover:bg-slate-50 hover:text-slate-900'
  }`

export default function Header() {
  const navigate = useNavigate()
  const { user, isLoggedIn, logout } = useAuthStore()
  const isManager = !!user && /ADMIN|MANAGER/.test(user.role)
  const showMyNav = !!user && !isManager

  const handleLogout = async () => {
    try {
      await logoutApi()
    } finally {
      logout()
      navigate('/')
    }
  }

  return (
    <>
      <header className="sticky top-0 z-40 border-b border-[var(--line)] bg-white/92 backdrop-blur">
        <div className="flex flex-col gap-2 px-4 py-2.5">
          <div className="flex items-center justify-between gap-4 min-[60rem]:hidden">
            <Link to="/" className="flex shrink-0 items-center whitespace-nowrap text-[22px] font-black leading-none tracking-tight text-slate-950">
              <span>Fest</span><span className="text-[var(--accent)]">ie</span>
            </Link>

            <div className="flex shrink-0 items-center gap-2">
              {user ? (
                <>
                  <Link
                    to="/my"
                    className="inline-flex items-center rounded-full border border-[var(--line)] bg-white px-3 py-2 text-sm font-semibold text-slate-700 transition-colors hover:bg-slate-50"
                  >
                    {user.nickname}
                  </Link>
                  <NotificationBell variant="mobile" />
                  <button
                    type="button"
                    onClick={handleLogout}
                    className="rounded-full bg-[var(--accent)] px-3 py-2 text-sm font-semibold text-white"
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

          <div className="relative hidden w-full items-center justify-between gap-6 min-[60rem]:flex">
            <Link to="/" className="flex shrink-0 items-center whitespace-nowrap text-[22px] font-black leading-none tracking-tight text-slate-950">
              <span>Fest</span><span className="text-[var(--accent)]">ie</span>
            </Link>

            <nav className="absolute left-1/2 top-1/2 flex min-w-0 -translate-x-1/2 -translate-y-1/2 items-center justify-center gap-5 whitespace-nowrap">
              <NavLink to="/" end className={navClass}>홈</NavLink>
              <NavLink to="/events" className={navClass}>행사</NavLink>
              <NavLink to="/community" className={navClass}>커뮤니티</NavLink>
              <NavLink to="/notices" className={navClass}>공지</NavLink>
              {showMyNav && <NavLink to="/my/calendars" className={navClass}>MY</NavLink>}
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

      <nav className="fixed inset-x-0 bottom-0 z-50 border-t border-[var(--line)] bg-white/95 px-3 py-2 shadow-[0_-8px_30px_rgba(15,23,42,0.08)] backdrop-blur min-[60rem]:hidden">
        <div className="mx-auto grid max-w-[680px] grid-cols-5 gap-2">
          <NavLink to="/" end className={mobileNavClass}>
            <MobileIcon name="home" />
            <span>홈</span>
          </NavLink>
          <NavLink to="/events" className={mobileNavClass}>
            <MobileIcon name="search" />
            <span>행사</span>
          </NavLink>
          <NavLink to="/community" className={mobileNavClass}>
            <MobileIcon name="community" />
            <span>커뮤니티</span>
          </NavLink>
          <NavLink to="/notices" className={mobileNavClass}>
            <MobileIcon name="notice" />
            <span>공지</span>
          </NavLink>
          <NavLink to={user ? (isManager ? '/admin' : '/my/calendars') : '/login'} className={mobileNavClass}>
            <MobileIcon name={user ? (isManager ? 'admin' : 'user') : 'user'} />
            <span>{user ? (isManager ? '관리' : 'MY') : '로그인'}</span>
          </NavLink>
        </div>
      </nav>
    </>
  )
}

function MobileIcon({ name }: { name: 'home' | 'search' | 'community' | 'calendar' | 'admin' | 'user' | 'notice' }) {
  const common = 'h-4.5 w-4.5'

  switch (name) {
    case 'home':
      return (
        <svg viewBox="0 0 24 24" className={common} fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <path d="M3 11.5 12 4l9 7.5" />
          <path d="M5 10.5V20h14v-9.5" />
        </svg>
      )
    case 'search':
      return (
        <svg viewBox="0 0 24 24" className={common} fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <circle cx="11" cy="11" r="6" />
          <path d="m20 20-3.5-3.5" />
        </svg>
      )
    case 'community':
      return (
        <svg viewBox="0 0 24 24" className={common} fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <path d="M7 11a4 4 0 1 1 8 0" />
          <path d="M4 19a8 8 0 0 1 16 0" />
          <path d="M17.5 8.5a3 3 0 1 1 0 6" />
        </svg>
      )
    case 'calendar':
      return (
        <svg viewBox="0 0 24 24" className={common} fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <rect x="3" y="5" width="18" height="16" rx="3" />
          <path d="M8 3v4M16 3v4M3 10h18" />
        </svg>
      )
    case 'admin':
      return (
        <svg viewBox="0 0 24 24" className={common} fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <path d="M12 2 4.5 6v6.5c0 4.6 3 7.9 7.5 9 4.5-1.1 7.5-4.4 7.5-9V6L12 2Z" />
          <path d="M9.5 12.2 11.2 14l3.3-3.5" />
        </svg>
      )
    case 'notice':
      return (
        <svg viewBox="0 0 24 24" className={common} fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <path d="M4 5.5h16v13H4z" />
          <path d="M7 9h10" />
          <path d="M7 12h10" />
          <path d="M7 15h6" />
        </svg>
      )
    case 'user':
    default:
      return (
        <svg viewBox="0 0 24 24" className={common} fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <circle cx="12" cy="8" r="3.5" />
          <path d="M5 20a7 7 0 0 1 14 0" />
        </svg>
      )
  }
}
