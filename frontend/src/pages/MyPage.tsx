import { useEffect, useState, type ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { updateMe } from '../api/auth'
import { useAuthStore } from '../store/authStore'

export default function MyPage() {
  const { user, setUser } = useAuthStore()
  const [nickname, setNickname] = useState(user?.nickname ?? '')
  const [phoneNumber, setPhoneNumber] = useState(user?.phoneNumber ?? '')
  const [saving, setSaving] = useState(false)
  const [saveMessage, setSaveMessage] = useState('')
  const [toast, setToast] = useState<{ type: 'success' | 'error'; message: string } | null>(null)

  useEffect(() => {
    setNickname(user?.nickname ?? '')
    setPhoneNumber(user?.phoneNumber ?? '')
  }, [user?.nickname, user?.phoneNumber])

  useEffect(() => {
    if (!toast) return
    const timer = window.setTimeout(() => setToast(null), 2200)
    return () => window.clearTimeout(timer)
  }, [toast])

  const saveNickname = async () => {
    if (!user) return
    const nextNickname = nickname.trim() || user.nickname
    setSaving(true)
    setSaveMessage('')
    try {
      const updated = await updateMe({
        name: user.name,
        nickname: nextNickname,
        phoneNumber: phoneNumber.trim() || user.phoneNumber || '',
      })
      setUser({
        ...user,
        nickname: updated.nickname ?? nextNickname,
        phoneNumber: updated.phoneNumber ?? phoneNumber ?? user.phoneNumber,
      })
      setNickname(updated.nickname ?? nextNickname)
      setPhoneNumber(updated.phoneNumber ?? phoneNumber)
      setSaveMessage('닉네임이 저장되었습니다.')
      setToast({ type: 'success', message: '프로필이 저장되었습니다.' })
    } catch {
      setSaveMessage('저장에 실패했습니다.')
      setToast({ type: 'error', message: '저장에 실패했습니다.' })
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="px-5 py-6 md:px-8 md:py-8">
      <div className="mx-auto max-w-4xl space-y-5">
        <section className="rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-6">
          <div className="inline-flex rounded-full bg-[var(--accent-soft)] px-3 py-1 text-xs font-semibold text-[var(--accent)]">마이페이지</div>
          <h1 className="mt-3 text-[24px] font-black tracking-tight text-slate-950 md:text-[28px]">
            {user?.nickname ?? '사용자'}님의 계정 정보
          </h1>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-600">
            닉네임을 먼저 바꿀 수 있게 두고, 휴대폰 인증과 활동 내역은 백엔드가 붙으면 이어갈 수 있어요.
          </p>
        </section>

        <section className="rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
          <div className="flex items-center justify-between gap-3">
            <div>
              <h2 className="text-[18px] font-black tracking-tight text-slate-950">기본 정보</h2>
              <p className="mt-1 text-sm text-slate-500">닉네임과 휴대폰 번호를 수정할 수 있어요.</p>
            </div>
            <div className="rounded-full bg-[var(--accent-soft)] px-3 py-1 text-xs font-semibold text-[var(--accent)]">
              프로필 수정
            </div>
          </div>

          <div className="mt-4 grid gap-4 md:grid-cols-[1fr_auto] md:items-end">
            <label className="block">
              <div className="mb-2 text-sm font-medium text-slate-700">닉네임</div>
              <input
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                className="w-full rounded-full border border-[var(--line)] bg-slate-50 px-4 py-3 text-sm outline-none focus:border-[var(--accent)]"
                placeholder="닉네임"
              />
            </label>
            <button
              type="button"
              onClick={saveNickname}
              disabled={saving}
              className="rounded-full border border-[var(--line)] bg-white px-4 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-50 disabled:opacity-70"
            >
              {saving ? '저장 중...' : '저장'}
            </button>
          </div>
          {saveMessage && <div className="mt-3 text-sm text-slate-500">{saveMessage}</div>}

          <div className="mt-5 grid gap-3 md:grid-cols-3">
            <MiniField label="이메일" value={user?.email ?? '-'} />
            <MiniField label="이름" value={user?.name ?? '-'} />
            <div className="rounded-[18px] border border-[var(--line)] bg-slate-50 px-4 py-3">
              <div className="text-[11px] font-semibold uppercase tracking-[0.12em] text-slate-500">휴대폰 번호</div>
              <input
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
                className="mt-2 w-full rounded-full border border-[var(--line)] bg-white px-4 py-3 text-sm outline-none focus:border-[var(--accent)]"
                placeholder="010-0000-0000"
              />
            </div>
          </div>
        </section>

        <div className="grid gap-4 md:grid-cols-2">
          <InfoCard title="내가 쓴 글 / 댓글" description="지금은 영역만 남겨두고, 목록 API가 생기면 바로 붙이기 좋습니다." />
          <InfoCard
            title="내가 저장한 일정"
            description="내 일정 캘린더로 바로 연결해서 추가한 행사만 따로 볼 수 있어요."
          >
            <Link
              to="/my/calendars"
              className="mt-4 inline-flex rounded-full border border-[var(--line)] bg-[var(--accent-soft)] px-4 py-2 text-sm font-semibold text-[var(--accent)]"
            >
              내 일정 열기
            </Link>
          </InfoCard>
        </div>
      </div>
      {toast && (
        <div
          className={`fixed bottom-5 right-5 z-50 rounded-2xl border px-4 py-3 text-sm shadow-[0_12px_30px_rgba(15,23,42,0.12)] ${
            toast.type === 'success'
              ? 'border-emerald-200 bg-emerald-50 text-emerald-700'
              : 'border-rose-200 bg-rose-50 text-rose-700'
          }`}
        >
          {toast.message}
        </div>
      )}
    </div>
  )
}

function InfoCard({ title, description, children }: { title: string; description: string; children?: ReactNode }) {
  return (
    <div className="rounded-[20px] border border-[var(--line)] bg-white p-4 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
      <div className="text-sm font-semibold text-slate-950">{title}</div>
      <p className="mt-2 text-sm leading-6 text-slate-500">{description}</p>
      {children}
    </div>
  )
}

function MiniField({ label, value, muted = false }: { label: string; value: string; muted?: boolean }) {
  return (
    <div className="rounded-[18px] border border-[var(--line)] bg-slate-50 px-4 py-3">
      <div className="text-[11px] font-semibold uppercase tracking-[0.12em] text-slate-500">{label}</div>
      <div className={`mt-1 text-sm font-semibold ${muted ? 'text-slate-400' : 'text-slate-900'}`}>{value}</div>
    </div>
  )
}
