import {useEffect, useMemo, useRef, useState} from 'react'
import {useMutation, useQuery} from '@tanstack/react-query'
import {askChatbot} from '../api/chatbot'
import {getEvents} from '../api/events'
import {getErrorMessage} from '../lib/error'
import {useAuthStore} from '../store/authStore'

type ChatRole = 'assistant' | 'user'

interface ChatMessage {
    id: string
    role: ChatRole
    content: string
    links?: Array<{ label: string; href: string }>
}

const QUICK_PROMPTS = [
    {label: '이용방법', kind: 'send', prompt: 'Festie 사이트 이용 방법을 알려줘.'},
    {label: '이번주 행사', kind: 'send', prompt: '이번 주 행사 알려줘.'},
    {label: '인기많은 행사', kind: 'send', prompt: '인기 많은 행사를 추천해줘. 찜 수 기준이 어렵다면 요즘 주목할 만한 행사도 알려줘.'},
    {label: '지역행사', kind: 'region'},
] as const

export default function ChatbotWidget() {
    const {user, accessToken} = useAuthStore()
    const userStorageKey = useMemo(() => `festie-chatbot:${user?.userId ?? 'guest'}`, [user?.userId])
    const defaultMessages = useMemo<ChatMessage[]>(() => ([
        {
            id: 'welcome',
            role: 'assistant',
            content: '👋 안녕하세요. Festie 챗봇입니다. 사이트 이용 방법이나 행사에 관련한 정보 탐색을 도와드려요.👀 \n\n무엇을 물어보시겠어요?',
        },
    ]), [])
    const [open, setOpen] = useState(false)
    const [input, setInput] = useState('')
    const [storageKey, setStorageKey] = useState(userStorageKey)
    const {data: eventCatalog = []} = useQuery({
        queryKey: ['chatbot-event-catalog'],
        queryFn: () => getEvents({size: 200}),
        staleTime: 10 * 60 * 1000,
    })
    const [messages, setMessages] = useState<ChatMessage[]>(defaultMessages)
    const bottomRef = useRef<HTMLDivElement | null>(null)
    const inputRef = useRef<HTMLTextAreaElement | null>(null)
    const hydratedRef = useRef(false)

    const greetingName = useMemo(() => user?.nickname ?? '방문자', [user?.nickname])

    useEffect(() => {
        const guestKey = 'festie-chatbot:guest'
        if (user?.userId) {
            const nextKey = `festie-chatbot:${user.userId}`
            if (storageKey !== nextKey) {
                if (window.localStorage.getItem(nextKey) == null) {
                    const guestRaw = window.localStorage.getItem(guestKey)
                    if (guestRaw) {
                        window.localStorage.setItem(nextKey, guestRaw)
                    }
                }
                setStorageKey(nextKey)
            }
            return
        }
        if (storageKey !== guestKey) {
            setStorageKey(guestKey)
        }
    }, [storageKey, user?.userId])

    useEffect(() => {
        hydratedRef.current = false
        try {
            const raw = window.localStorage.getItem(storageKey)
            if (!raw) {
                setOpen(false)
                setInput('')
                setMessages(defaultMessages)
                return
            }
            const parsed = JSON.parse(raw) as {
                open?: boolean
                input?: string
                messages?: ChatMessage[]
            }
            if (typeof parsed.open === 'boolean') {
                setOpen(parsed.open)
            }
            if (typeof parsed.input === 'string') {
                setInput(parsed.input)
            }
            if (Array.isArray(parsed.messages) && parsed.messages.length > 0) {
                setMessages(parsed.messages.slice(-30))
            } else {
                setMessages(defaultMessages)
            }
        } catch {
            setOpen(false)
            setInput('')
            setMessages(defaultMessages)
        } finally {
            hydratedRef.current = true
        }
    }, [defaultMessages, storageKey])

    useEffect(() => {
        if (!hydratedRef.current) return
        try {
            window.localStorage.setItem(
                storageKey,
                JSON.stringify({
                    open,
                    input,
                    messages: messages.slice(-30),
                })
            )
        } catch {
            // 저장 공간이 부족하면 조용히 무시
        }
    }, [storageKey, open, input, messages])

    useEffect(() => {
        bottomRef.current?.scrollIntoView({behavior: 'smooth', block: 'end'})
    }, [messages, open])

    useEffect(() => {
        if (open) {
            window.setTimeout(() => inputRef.current?.focus(), 0)
        }
    }, [open])

    const mutation = useMutation({
        mutationFn: (question: string) => askChatbot(question),
        onSuccess: (answer) => {
            const normalized = normalizeAssistantMessage(answer, eventCatalog)
            setMessages((prev) => [
                ...prev,
                normalized,
            ])
            setInput('')
        },
        onError: (error) => {
            const message = getErrorMessage(error, '잠시 후 다시 시도해 주세요.')
            setMessages((prev) => [
                ...prev,
                {
                    id: `assistant-${Date.now()}`,
                    role: 'assistant',
                    content: message.includes('401') || message.toLowerCase().includes('unauthorized')
                        ? '로그인 후 이용해 주세요. 로그인하면 행사 추천과 질문 답변을 받을 수 있어요.'
                        : message,
                },
            ])
        },
    })

    const sendQuestion = async (question: string) => {
        const normalized = question.trim()
        if (!normalized || mutation.isPending) return
        if (!accessToken || !user) {
            setMessages((prev) => [
                ...prev,
                {
                    id: `assistant-${Date.now()}`,
                    role: 'assistant',
                    content: '로그인 후 이용해 주세요. 로그인하면 행사 추천과 질문 답변을 받을 수 있어요.',
                },
            ])
            return
        }
        setMessages((prev) => [
            ...prev,
            {id: `user-${Date.now()}`, role: 'user', content: normalized},
        ])
        try {
            await mutation.mutateAsync(normalized)
        } catch {
            // onError에서 메시지를 보여줍니다.
        }
    }

    const askRegion = () => {
        setMessages((prev) => [
            ...prev,
            {
                id: `assistant-${Date.now()}`,
                role: 'assistant',
                content: '어느 지역 행사를 찾으시나요? 예: 서울, 부산, 대구, 인천',
            },
        ])
        window.setTimeout(() => inputRef.current?.focus(), 0)
    }

    return (
        <>
            {!open ? (
                <button
                    type="button"
                    onClick={() => setOpen(true)}
                    className="fixed bottom-24 right-4 z-[60] inline-flex items-center gap-2 rounded-full border border-slate-200 bg-slate-950 px-4 py-3 text-sm font-semibold text-white shadow-[0_16px_40px_rgba(15,23,42,0.24)] transition-transform hover:scale-[1.02] lg:bottom-4"
                >
                    <span
                        className="inline-flex h-8 w-8 items-center justify-center rounded-full bg-white/10 text-base">AI</span>
                    <span className="hidden lg:inline">Festie 챗봇</span>
                </button>
            ) : (
                <>
                    <div
                        className="fixed inset-0 z-[50] bg-transparent"
                        onClick={() => setOpen(false)}
                        aria-hidden="true"
                    />
                    <div
                        className="fixed bottom-20 right-3 z-[60] flex h-[min(74vh,560px)] w-[min(88vw,380px)] flex-col overflow-hidden rounded-[24px] border border-slate-200 bg-white/90 shadow-[0_24px_60px_rgba(15,23,42,0.24)] backdrop-blur-xl min-[60rem]:bottom-4 min-[60rem]:right-4 min-[60rem]:h-[600px] min-[60rem]:w-[min(92vw,420px)]">
                    <div
                        className="flex items-start justify-between gap-3 bg-gradient-to-r from-slate-950 via-slate-900 to-slate-800 px-4 py-3 text-white">
                        <div>
                            <div
                                className="inline-flex rounded-full bg-white/10 px-2.5 py-1 text-[11px] font-semibold text-slate-100">
                                RAG 챗봇
                            </div>
                            <div className="mt-1 text-[16px] font-black tracking-tight">Festie AI</div>
                            <div className="mt-0.5 text-[11px] text-slate-300">안녕하세요, {greetingName}님</div>
                        </div>
                        <button
                            type="button"
                            onClick={() => setOpen(false)}
                            className="rounded-full bg-white/10 px-2.5 py-1.5 text-xs font-semibold text-white"
                        >
                            닫기
                        </button>
                    </div>

                    <div
                        className="flex min-h-0 flex-1 flex-col bg-[linear-gradient(180deg,rgba(248,250,252,0.92)_0%,rgba(255,255,255,0.92)_100%)]">
                        <div
                            className="grid grid-cols-4 gap-1.5 border-b border-transparent bg-transparent px-4 py-2.5">
                            {QUICK_PROMPTS.map((item) => (
                <button
                    key={item.label}
                    type="button"
                                    onClick={() => {
                                        if (item.kind === 'region') {
                                            askRegion()
                                            return
                                        }
                                        void sendQuestion(item.prompt)
                                    }}
                                    className="group flex min-h-[40px] flex-col justify-center rounded-[20px] border border-[rgba(138,109,255,0.16)] bg-[linear-gradient(180deg,rgba(139,92,246,0.16)_0%,rgba(165,180,252,0.08)_100%)] px-2 py-1 text-left text-[11px] font-semibold text-slate-700 shadow-[inset_0_1px_0_rgba(255,255,255,0.72),0_8px_20px_rgba(91,77,255,0.08)] backdrop-blur-md transition-all hover:-translate-y-0.5 hover:border-[rgba(138,109,255,0.26)] hover:bg-[linear-gradient(180deg,rgba(139,92,246,0.2)_0%,rgba(165,180,252,0.12)_100%)]"
                                >
                                    <span className="block text-center text-[11px] font-medium text-slate-800">{item.label}</span>
                                </button>
                            ))}
                        </div>

                        <div className="min-h-0 flex-1 space-y-3 overflow-y-auto px-4 py-4 pr-3">
                            {messages.map((message) => (
                                <div key={message.id}
                                     className={`flex ${message.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                                    <div
                                        className={`max-w-[92%] rounded-[18px] px-3.5 py-3 text-sm leading-6 shadow-sm ${
                                            message.role === 'user'
                                                ? 'rounded-br-md bg-[var(--accent)] text-white'
                                                : 'rounded-bl-md border border-slate-200 bg-white/95 text-slate-800'
                                        }`}
                                    >
                                        <div className="whitespace-pre-wrap break-words text-[13px] leading-5">
                                            {message.content}
                                        </div>
                                        {message.links?.length ? (
                                            <div className="mt-2 flex flex-wrap gap-2">
                                                {message.links.map((link) => (
                                                    <a
                                                        key={link.href}
                                                        href={link.href}
                                                        target={link.href.startsWith('http') ? '_blank' : '_self'}
                                                        rel={link.href.startsWith('http') ? 'noreferrer' : undefined}
                                                        className="inline-flex rounded-full border border-violet-200 bg-white px-3 py-1 text-[11px] font-semibold text-violet-700 transition-colors hover:bg-violet-50"
                                                    >
                                                        {link.label}
                                                    </a>
                                                ))}
                                            </div>
                                        ) : null}
                                    </div>
                                </div>
                            ))}
                            {mutation.isPending && (
                                <div className="flex justify-start">
                                    <div
                                        className="rounded-[18px] rounded-bl-md border border-slate-200 bg-white/95 px-3.5 py-3 text-sm text-slate-500 shadow-sm">
                                        답변을 찾고 있어요...
                                    </div>
                                </div>
                            )}
                            <div ref={bottomRef}/>
                        </div>

                        <div className="border-t border-slate-200/70 bg-white/90 p-3 backdrop-blur-md">
              <textarea
                  ref={inputRef}
                  value={input}
                  onChange={(e) => setInput(e.target.value)}
                  onKeyDown={(e) => {
                      if (e.key === 'Enter' && !e.shiftKey) {
                          e.preventDefault()
                          void sendQuestion(input)
                      }
                  }}
                  rows={3}
                  placeholder="질문을 입력하세요. 예: 서울 지역 행사 추천해줘"
                  className="w-full resize-none rounded-[18px] border border-slate-200/80 bg-slate-50/90 px-3 py-3 text-sm outline-none placeholder:text-slate-400 focus:border-[var(--accent-soft)]"
              />
                            <div className="mt-2 flex items-center justify-between gap-2">
                <span className="text-[11px] text-slate-400">Enter로 전송, Shift+Enter로 줄바꿈</span>
                <button
                    type="button"
                    onClick={() => void sendQuestion(input)}
                    disabled={mutation.isPending || !input.trim()}
                    className="rounded-full bg-[var(--accent)] px-4 py-1.5 text-sm font-semibold text-white disabled:opacity-50"
                >
                    보내기
                </button>
                            </div>
                        </div>
                    </div>
                    </div>
                </>
            )}
        </>
    )
}

function normalizeAssistantMessage(answer: string, eventCatalog: Array<{ id: string; name: string }>): ChatMessage {
    const raw = String(answer ?? '').trim()
    const compact = compactAssistantText(raw)
    const matchingEvents = findMatchingEvents(raw, eventCatalog)
    const links = matchingEvents.length > 0
        ? matchingEvents.length === 1
            ? [{
                label: '상세 페이지 바로가기',
                href: new URL(`/events/${matchingEvents[0].id}`, window.location.origin).toString(),
            }]
            : [{
                label: '행사 조회 페이지',
                href: new URL('/events', window.location.origin).toString(),
            }]
        : undefined

    return {
        id: `assistant-${Date.now()}`,
        role: 'assistant',
        content: compact,
        links,
    }
}

function compactAssistantText(raw: string) {
    const withoutMarkdown = raw.replace(/\*\*/g, '').trim()
    const eventSummary = compactEventSummary(withoutMarkdown)
    if (eventSummary) return eventSummary

    return withoutMarkdown
        .replace(/\s*👉\s*\[?상세\s*페이지\]?\([^)]+\)/g, '')
        .replace(/👉\s*상세\s*페이지[:：]?\s*/g, '')
        .replace(/https?:\/\/\S+/g, '')
        .replace(/\/v1\/events\/[0-9a-fA-F-]{8,}/g, '')
        .replace(/\/events\/[0-9a-fA-F-]{8,}/g, '')
        .replace(/^현재 진행 중인 행사는 다음과 같습니다[:：]?\s*/g, '')
        .replace(/\s*(\d+\.)\s*/g, '\n$1 ')
        .replace(/\n{3,}/g, '\n\n')
        .replace(/[ \t]+/g, ' ')
        .trim()
}

function compactEventSummary(raw: string) {
  const fields = ['행사명', '날짜'] as const

    const extracted = fields.map((label) => {
        const nextPattern = fields.filter((item) => item !== label).join('|')
        const regex = new RegExp(`${label}\\s*[:：]\\s*([\\s\\S]*?)(?=\\s+(?:${nextPattern})\\s*[:：]|\\s*👉\\s*상세\\s*페이지|$)`)
        const match = raw.match(regex)
        return [label, match?.[1]?.trim() ?? ''] as const
    })

    const hasAnyField = extracted.some(([, value]) => Boolean(value))
    if (!hasAnyField) return null

    const summaryParts = ['행사']
    const byLabel = new Map(extracted)

    const name = byLabel.get('행사명')
    const date = byLabel.get('날짜')
    if (name) summaryParts.push(name)
    if (date) summaryParts.push(date)

  return summaryParts.join(' · ')
}

function findMatchingEvents(text: string, eventCatalog: Array<{ id: string; name: string }>) {
    const normalized = text.toLowerCase()
    return eventCatalog.filter((event) => {
        const name = String(event.name ?? '').toLowerCase()
        return name && normalized.includes(name)
    }).slice(0, 5)
}
