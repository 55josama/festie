import { useMutation, useQuery } from '@tanstack/react-query'
import { useEffect, useMemo, useRef, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { createNotice, getNotice, updateNotice } from '../api/notices'
import { useAuthStore } from '../store/authStore'

const ALLOWED_TAGS = new Set(['B', 'STRONG', 'I', 'EM', 'U', 'S', 'STRIKE', 'BR', 'P', 'DIV', 'UL', 'OL', 'LI', 'A', 'SPAN'])

function sanitizeHtml(input: string) {
  if (typeof window === 'undefined') return input
  const parser = new DOMParser()
  const doc = parser.parseFromString(`<div>${input}</div>`, 'text/html')

  const sanitizeNode = (node: Node): Node | null => {
    if (node.nodeType === Node.TEXT_NODE) return node.cloneNode(true)
    if (node.nodeType !== Node.ELEMENT_NODE) return null

    const element = node as HTMLElement
    const tagName = element.tagName.toUpperCase()
    if (!ALLOWED_TAGS.has(tagName)) {
      const fragment = document.createDocumentFragment()
      element.childNodes.forEach((child) => {
        const sanitizedChild = sanitizeNode(child)
        if (sanitizedChild) fragment.appendChild(sanitizedChild)
      })
      return fragment
    }

    const clone = document.createElement(tagName.toLowerCase())
    if (tagName === 'A') {
      const href = element.getAttribute('href') ?? ''
      if (href.startsWith('http://') || href.startsWith('https://') || href.startsWith('/') || href.startsWith('#')) {
        clone.setAttribute('href', href)
        clone.setAttribute('rel', 'noreferrer noopener')
        clone.setAttribute('target', '_blank')
      }
    }

    element.childNodes.forEach((child) => {
      const sanitizedChild = sanitizeNode(child)
      if (sanitizedChild) clone.appendChild(sanitizedChild)
    })
    return clone
  }

  const wrapper = document.createElement('div')
  doc.body.firstChild?.childNodes.forEach((child) => {
    const sanitizedChild = sanitizeNode(child)
    if (sanitizedChild) wrapper.appendChild(sanitizedChild)
  })
  return wrapper.innerHTML
}

function stripHtml(html: string) {
  if (typeof window === 'undefined') return html.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
  const div = document.createElement('div')
  div.innerHTML = html
  return (div.textContent ?? '').replace(/\s+/g, ' ').trim()
}

function execFormat(command: string, value?: string) {
  document.execCommand(command, false, value)
}

export default function NoticeWrite() {
  const { noticeId = '' } = useParams()
  const navigate = useNavigate()
  const { user } = useAuthStore()
  const canWriteNotice = !!user && (user.role === 'ADMIN' || user.role === 'MANAGER' || /_MANAGER$/.test(String(user.role ?? '')))
  const isEditMode = Boolean(noticeId)
  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const editorRef = useRef<HTMLDivElement | null>(null)

  const { data: notice } = useQuery({
    queryKey: ['notice', noticeId],
    queryFn: () => getNotice(noticeId),
    enabled: !!noticeId && canWriteNotice,
  })

  useEffect(() => {
    if (!notice || !isEditMode) return
    setTitle(notice.title ?? '')
    setContent(sanitizeHtml(notice.content ?? ''))
  }, [isEditMode, notice])

  useEffect(() => {
    if (!editorRef.current) return
    const currentHtml = editorRef.current.innerHTML
    if (currentHtml !== content) {
      editorRef.current.innerHTML = content
    }
  }, [content])

  const saveNoticeMutation = useMutation({
    mutationFn: () => {
      const normalizedContent = sanitizeHtml(content).trim()
      if (isEditMode) {
        return updateNotice(noticeId, title.trim(), normalizedContent)
      }
      return createNotice(title.trim(), normalizedContent)
    },
    onSuccess: (savedNotice) => {
      navigate(`/notices/${savedNotice.noticeId}`)
    },
  })

  const submitLabel = useMemo(() => {
    if (saveNoticeMutation.isPending) return isEditMode ? '수정 중...' : '작성 중...'
    return isEditMode ? '공지 수정' : '공지 등록'
  }, [isEditMode, saveNoticeMutation.isPending])

  if (!canWriteNotice) {
    return (
      <div className="px-5 py-10 text-slate-500">
        공지 작성 권한이 없습니다.
      </div>
    )
  }

  const setEditorHtml = (html: string) => {
    setContent(html)
  }

  const insertLink = () => {
    const url = window.prompt('링크 주소를 입력해 주세요.')
    if (!url) return
    execFormat('createLink', url)
    if (editorRef.current) setContent(editorRef.current.innerHTML)
  }

  return (
    <div className="space-y-4 px-5 py-5 md:px-8 md:py-7">
      <div className="mb-1 flex items-center gap-2 text-sm text-slate-500">
        <Link to="/" className="hover:text-slate-900">홈</Link>
        <span>/</span>
        <Link to="/notices" className="hover:text-slate-900">공지</Link>
        <span>/</span>
        <span className="text-slate-900">{isEditMode ? '공지 수정' : '공지 작성'}</span>
      </div>

      <section className="rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-6">
        <div className="inline-flex rounded-full bg-[var(--accent-soft)] px-3 py-1 text-[11px] font-semibold text-[var(--accent)]">
          {isEditMode ? '공지 수정' : '공지 작성'}
        </div>
        <h1 className="mt-3 text-[24px] font-black tracking-tight text-slate-950">
          {isEditMode ? '공지사항을 수정해요' : '새 공지사항을 작성해요'}
        </h1>
        <p className="mt-1 text-sm text-slate-500">관리자와 매니저만 작성할 수 있어요.</p>

        <div className="mt-5 space-y-3">
          <input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="공지 제목"
            className="w-full rounded-[18px] border border-[var(--line)] bg-white px-4 py-3 text-sm outline-none"
          />

          <div className="overflow-hidden rounded-[18px] border border-[var(--line)] bg-white">
            <div className="flex flex-wrap items-center gap-2 border-b border-[var(--line)] bg-slate-50 px-3 py-2">
              <button type="button" onMouseDown={(e) => e.preventDefault()} onClick={() => execFormat('bold')} className="rounded-full border border-[var(--line)] bg-white px-3 py-1 text-xs font-semibold text-slate-700 hover:bg-slate-100">B</button>
              <button type="button" onMouseDown={(e) => e.preventDefault()} onClick={() => execFormat('italic')} className="rounded-full border border-[var(--line)] bg-white px-3 py-1 text-xs italic text-slate-700 hover:bg-slate-100">I</button>
              <button type="button" onMouseDown={(e) => e.preventDefault()} onClick={() => execFormat('underline')} className="rounded-full border border-[var(--line)] bg-white px-3 py-1 text-xs underline text-slate-700 hover:bg-slate-100">U</button>
              <button type="button" onMouseDown={(e) => e.preventDefault()} onClick={() => execFormat('strikeThrough')} className="rounded-full border border-[var(--line)] bg-white px-3 py-1 text-xs line-through text-slate-700 hover:bg-slate-100">S</button>
              <button type="button" onMouseDown={(e) => e.preventDefault()} onClick={insertLink} className="rounded-full border border-[var(--line)] bg-white px-3 py-1 text-xs font-semibold text-slate-700 hover:bg-slate-100">Link</button>
              <select
                defaultValue=""
                onChange={(e) => {
                  const value = e.target.value
                  if (value) {
                    execFormat('fontSize', value)
                    if (editorRef.current) setContent(editorRef.current.innerHTML)
                  }
                }}
                className="rounded-full border border-[var(--line)] bg-white px-3 py-1 text-xs outline-none"
              >
                <option value="">크기</option>
                <option value="1">작게</option>
                <option value="3">기본</option>
                <option value="5">크게</option>
              </select>
            </div>
            <div
              ref={editorRef}
              contentEditable
              suppressContentEditableWarning
              onInput={(e) => setEditorHtml((e.currentTarget as HTMLDivElement).innerHTML)}
              onBlur={(e) => setEditorHtml((e.currentTarget as HTMLDivElement).innerHTML)}
              className="min-h-[240px] w-full px-4 py-3 text-sm leading-7 outline-none"
              style={{ whiteSpace: 'pre-wrap' }}
            />
          </div>

          <div className="text-[11px] text-slate-500">
            굵게, 기울임, 밑줄, 취소선, 링크, 글자 크기만 지원해요.
          </div>

          <div className="flex items-center justify-end gap-2">
            <Link
              to={isEditMode ? `/notices/${noticeId}` : '/notices'}
              className="rounded-full border border-[var(--line)] bg-white px-4 py-2.5 text-sm font-semibold text-slate-700"
            >
              취소
            </Link>
            <button
              type="button"
              onClick={() => saveNoticeMutation.mutate()}
              disabled={!title.trim() || !stripHtml(content).trim() || saveNoticeMutation.isPending}
              className="rounded-full bg-[var(--accent)] px-5 py-2.5 text-sm font-semibold text-white disabled:opacity-50"
            >
              {submitLabel}
            </button>
          </div>
        </div>
      </section>
    </div>
  )
}
