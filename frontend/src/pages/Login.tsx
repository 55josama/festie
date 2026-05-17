import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getMe, login } from '../api/auth'
import { buildUserFromToken } from '../lib/jwt'
import { getErrorMessage } from '../lib/error'
import { useAuthStore } from '../store/authStore'

export default function Login() {
  const navigate = useNavigate()
  const { setAccessToken, setRefreshToken, setUser, syncUserFromAccessToken } = useAuthStore()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const submit = async (e: FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      const result = await login(email, password)
      if (result?.accessToken) setAccessToken(result.accessToken)
      if (result?.refreshToken) setRefreshToken(result.refreshToken)
      if (result?.accessToken) {
        const fallbackUser = buildUserFromToken(result.accessToken)
        if (fallbackUser) setUser(fallbackUser)
      }
      const me = await getMe().catch(() => null)
      if (me) setUser(me)
      else syncUserFromAccessToken()
      navigate('/')
    } catch (err: any) {
      setError(getErrorMessage(err, '로그인에 실패했습니다.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="grid min-h-[calc(100vh-120px)] place-items-center px-5 py-8">
      <div className="grid w-full max-w-5xl overflow-hidden rounded-[30px] border border-[var(--line)] bg-white shadow-[0_16px_40px_rgba(15,23,42,0.08)] lg:grid-cols-[1fr_0.9fr]">
        <div className="hidden bg-[linear-gradient(135deg,rgba(111,84,255,0.98),rgba(81,55,230,0.96))] p-8 text-white lg:block">
          <div className="inline-flex rounded-full bg-white/10 px-3 py-1 text-sm font-semibold text-slate-200">환영합니다</div>
          <h1 className="mt-5 text-[38px] font-black tracking-tight">티켓팅과 커뮤니티를 가장 간단하게.</h1>
          <p className="mt-4 max-w-md text-[16px] leading-7 text-slate-300">
            한 번 로그인하면 행사 탐색, 채팅 참여, 일정 저장을 바로 이어서 사용할 수 있어요.
          </p>
        </div>

        <form onSubmit={submit} className="space-y-5 p-8">
          <div>
            <h2 className="text-[28px] font-black tracking-tight text-slate-950">로그인</h2>
            <p className="mt-2 text-sm text-slate-500">서비스를 계속 사용하려면 계정으로 들어오세요.</p>
          </div>

          <Input label="이메일" value={email} onChange={setEmail} type="email" placeholder="example@festie.kr" />
          <Input label="비밀번호" value={password} onChange={setPassword} type="password" placeholder="비밀번호를 입력하세요" />

          {error && <div className="rounded-2xl bg-rose-50 px-4 py-3 text-sm text-rose-700">{error}</div>}

          <button
            disabled={loading}
            className="w-full rounded-full border border-[var(--line)] bg-[var(--accent-soft)] px-5 py-3 text-sm font-semibold text-[var(--accent)] disabled:opacity-70"
          >
            {loading ? '로그인 중...' : '로그인'}
          </button>

          <div className="text-sm text-slate-500">
            아직 계정이 없다면 <Link to="/register" className="font-semibold text-[var(--accent)]">회원가입</Link>
          </div>
        </form>
      </div>
    </div>
  )
}

function Input({
  label,
  value,
  onChange,
  type,
  placeholder,
}: {
  label: string
  value: string
  onChange: (value: string) => void
  type: string
  placeholder?: string
}) {
  return (
    <label className="block">
      <div className="mb-2 text-sm font-medium text-slate-700">{label}</div>
      <input
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        className="w-full rounded-full border border-[var(--line)] bg-slate-50 px-4 py-3 text-sm outline-none focus:border-[var(--accent)]"
      />
    </label>
  )
}
