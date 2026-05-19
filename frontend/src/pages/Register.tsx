import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { register } from '../api/auth'
import { getErrorMessage } from '../lib/error'

export default function Register() {
  const navigate = useNavigate()
  const [form, setForm] = useState({
    email: '',
    password: '',
    name: '',
    nickname: '',
    phone1: '010',
    phone2: '',
    phone3: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const submit = async (e: FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      await register({
        email: form.email,
        password: form.password,
        name: form.name,
        nickname: form.nickname,
        phoneNumber: joinPhoneNumber(form.phone1, form.phone2, form.phone3),
      })
      navigate('/login')
    } catch (err: any) {
      setError(getErrorMessage(err, '회원가입에 실패했습니다.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="grid min-h-[calc(100vh-120px)] place-items-center px-5 py-8">
      <div className="grid w-full max-w-5xl overflow-hidden rounded-[30px] border border-[var(--line)] bg-white shadow-[0_16px_40px_rgba(15,23,42,0.08)] lg:grid-cols-[0.9fr_1fr]">
        <div className="hidden bg-[linear-gradient(135deg,rgba(111,84,255,0.98),rgba(84,116,255,0.92))] p-8 text-white lg:block">
          <div className="inline-flex rounded-full bg-white/15 px-3 py-1 text-sm font-semibold text-white/90">회원가입</div>
          <h1 className="mt-5 text-[38px] font-black tracking-tight">내가 좋아하는 행사만 모아보세요.</h1>
          <p className="mt-4 max-w-md text-[16px] leading-7 text-white/85">
            관심 있는 공연을 일정에 담고, 커뮤니티와 채팅으로 바로 이어갈 수 있습니다.
          </p>
        </div>

        <form onSubmit={submit} className="space-y-5 p-8">
          <div>
            <h2 className="text-[28px] font-black tracking-tight text-slate-950">회원가입</h2>
            <p className="mt-2 text-sm text-slate-500">계정을 만들고 Festie를 시작하세요.</p>
          </div>

          <GridInput label="이메일" value={form.email} onChange={(email) => setForm((prev) => ({ ...prev, email }))} type="email" />
          <GridInput label="비밀번호" value={form.password} onChange={(password) => setForm((prev) => ({ ...prev, password }))} type="password" />
          <GridInput label="이름" value={form.name} onChange={(name) => setForm((prev) => ({ ...prev, name }))} />
          <GridInput label="닉네임" value={form.nickname} onChange={(nickname) => setForm((prev) => ({ ...prev, nickname }))} />
          <PhoneNumberInput
            phone1={form.phone1}
            phone2={form.phone2}
            phone3={form.phone3}
            onChange={(next) => setForm((prev) => ({ ...prev, ...next }))}
          />

          {error && <div className="rounded-2xl bg-rose-50 px-4 py-3 text-sm text-rose-700">{error}</div>}

          <button
            disabled={loading}
            className="w-full rounded-full border border-[var(--line)] bg-[var(--accent-soft)] px-5 py-3 text-sm font-semibold text-[var(--accent)] disabled:opacity-70"
          >
            {loading ? '가입 중...' : '회원가입'}
          </button>

          <div className="text-sm text-slate-500">
            이미 계정이 있다면 <Link to="/login" className="font-semibold text-[var(--accent)]">로그인</Link>
          </div>
        </form>
      </div>
    </div>
  )
}

function GridInput({
  label,
  value,
  onChange,
  type = 'text',
}: {
  label: string
  value: string
  onChange: (value: string) => void
  type?: string
}) {
  return (
    <label className="block">
      <div className="mb-2 text-sm font-medium text-slate-700">{label}</div>
      <input
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="w-full rounded-full border border-[var(--line)] bg-slate-50 px-4 py-3 text-sm outline-none focus:border-[var(--accent)]"
      />
    </label>
  )
}

function PhoneNumberInput({
  phone1,
  phone2,
  phone3,
  onChange,
}: {
  phone1: string
  phone2: string
  phone3: string
  onChange: (value: { phone1?: string; phone2?: string; phone3?: string }) => void
}) {
  return (
    <label className="block">
      <div className="mb-2 text-sm font-medium text-slate-700">전화번호</div>
      <div className="grid grid-cols-[80px_1fr_1fr] gap-2">
        <input
          value={phone1}
          onChange={(e) => onChange({ phone1: e.target.value.replace(/\D/g, '').slice(0, 3) })}
          maxLength={3}
          inputMode="numeric"
          className="w-full rounded-full border border-[var(--line)] bg-slate-50 px-4 py-3 text-sm outline-none focus:border-[var(--accent)]"
        />
        <input
          value={phone2}
          onChange={(e) => onChange({ phone2: e.target.value.replace(/\D/g, '').slice(0, 4) })}
          maxLength={4}
          inputMode="numeric"
          className="w-full rounded-full border border-[var(--line)] bg-slate-50 px-4 py-3 text-sm outline-none focus:border-[var(--accent)]"
        />
        <input
          value={phone3}
          onChange={(e) => onChange({ phone3: e.target.value.replace(/\D/g, '').slice(0, 4) })}
          maxLength={4}
          inputMode="numeric"
          className="w-full rounded-full border border-[var(--line)] bg-slate-50 px-4 py-3 text-sm outline-none focus:border-[var(--accent)]"
        />
      </div>
    </label>
  )
}

function joinPhoneNumber(phone1: string, phone2: string, phone3: string) {
  const first = phone1.trim() || '010'
  const second = phone2.trim()
  const third = phone3.trim()
  if (!second || !third) return first
  return `${first}-${second}-${third}`
}
