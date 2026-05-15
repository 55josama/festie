import { type ReactNode, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getCategories, getPosts } from '../api/community'
import { approveEventRequest, rejectEventRequest, updateOperationRequestStatus } from '../api/admin'
import { getEventRequests, getMyEventRequests, getMyOperationRequests, getOperationRequests } from '../api/requests'
import PostCard from '../components/PostCard'
import RequestCard from '../components/RequestCard'
import { useAuthStore } from '../store/authStore'
import type { EventRequestItem, OperationRequestItem } from '../types/admin'

type FeedTab = 'posts' | 'requests'
type RequestKind = 'event' | 'operation'

type RequestFeedItem = {
    id: string
    kind: 'event' | 'operation'
    kindLabel: string
    metaLabel: string
    title: string
    body: string
    authorNickname?: string | null
    createdAt?: string | null
    status: string
    rejectReason?: string | null
    statusClassName: string
}

export default function Community() {
    const { user } = useAuthStore()
    const queryClient = useQueryClient()
    const [feedTab, setFeedTab] = useState<FeedTab>('posts')
    const [requestKind, setRequestKind] = useState<RequestKind>('event')
    const [categoryId, setCategoryId] = useState<string | undefined>()
    const [sort, setSort] = useState<'latest' | 'popular'>('latest')
    const [requestRejectReasons, setRequestRejectReasons] = useState<Record<string, string>>({})
    const isAdmin = !!user && /ADMIN/.test(user.role)
    const isManager = !!user && /_MANAGER$/.test(user.role)
    const canViewAllRequests = isAdmin || isManager
    const canViewOperationRequests = isAdmin
    const canModerateEventRequests = isAdmin || isManager

    const {data: categories = []} = useQuery({
        queryKey: ['categories'],
        queryFn: getCategories,
    })

    const {data: rawPosts = []} = useQuery({
        queryKey: ['posts', categoryId, sort],
        queryFn: () => getPosts({categoryId, sort: sort === 'popular' ? 'likeCount,desc' : 'createdAt,desc', size: 100}),
        enabled: feedTab === 'posts',
    })

    const {data: eventRequests = []} = useQuery<EventRequestItem[]>({
        queryKey: ['community', 'event-requests', canViewAllRequests],
        queryFn: () => canViewAllRequests
            ? getEventRequests({size: 100})
            : getMyEventRequests({size: 100}),
        enabled: feedTab === 'requests',
    })

    const {data: operationRequests = []} = useQuery<OperationRequestItem[]>({
        queryKey: ['community', 'operation-requests', canViewOperationRequests],
        queryFn: () => canViewAllRequests
            ? getOperationRequests({size: 100})
            : getMyOperationRequests({size: 100}),
        enabled: feedTab === 'requests' && canViewOperationRequests,
    })

    const approveEventMutation = useMutation({
        mutationFn: (requestId: string) => approveEventRequest(requestId),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: ['community', 'event-requests']})
        },
    })

    const rejectEventMutation = useMutation({
        mutationFn: ({requestId, reason}: { requestId: string; reason: string }) => rejectEventRequest(requestId, reason),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: ['community', 'event-requests']})
        },
    })

    const updateOperationStatusMutation = useMutation({
        mutationFn: ({requestId, status, reason}: { requestId: string; status: string; reason: string }) =>
            updateOperationRequestStatus(requestId, status, reason),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: ['community', 'operation-requests']})
        },
    })

    const categoryNameById = useMemo(() => {
        return new Map(categories.map((category: any) => [category.id, category.name]))
    }, [categories])

    const posts = useMemo(() => {
        if (sort === 'latest') return rawPosts
        const formatter = new Intl.DateTimeFormat('ko-KR', { timeZone: 'Asia/Seoul' })
        const today = formatter.format(new Date())
        const todayPosts = [...rawPosts]
            .filter((post: any) => {
                const createdAt = new Date(post.createdAt)
                if (Number.isNaN(createdAt.getTime())) return false
                return formatter.format(createdAt) === today
            })
            .sort((a: any, b: any) => b.likeCount - a.likeCount)
        return todayPosts.length ? todayPosts : [...rawPosts].sort((a: any, b: any) => b.likeCount - a.likeCount)
    }, [rawPosts, sort])

    const requestItems = useMemo<RequestFeedItem[]>(() => {
        const eventRequestCards = (eventRequests ?? []).map((request: any) => ({
            id: request.id,
            kind: 'event' as const,
            kindLabel: '요청',
            metaLabel: '이벤트 요청',
            title: request.eventName ?? '',
            body: request.description ?? '',
            authorNickname: request.requesterNickname ?? null,
            createdAt: request.createdAt,
            status: request.status ?? 'PENDING',
            rejectReason: request.rejectReason,
            statusClassName: requestStatusClass(request.status),
        }))

        const operationRequestCards = (operationRequests ?? []).map((request: any) => ({
            id: request.id,
            kind: 'operation' as const,
            kindLabel: '요청',
            metaLabel: '운영 요청',
            title: request.title ?? '운영 요청',
            body: request.content ?? '',
            authorNickname: request.requesterNickname ?? null,
            createdAt: request.createdAt,
            status: request.status ?? 'PENDING',
            rejectReason: request.adminMemo,
            statusClassName: requestStatusClass(request.status),
        }))

        const combined = [...eventRequestCards, ...operationRequestCards]
        const filtered = requestKind === 'event'
            ? combined.filter((request) => request.kind === 'event')
            : combined.filter((request) => request.kind === 'operation')
        return filtered.sort(
            (left, right) => new Date(right.createdAt ?? 0).getTime() - new Date(left.createdAt ?? 0).getTime(),
        )
    }, [eventRequests, operationRequests, requestKind])

    return (
        <div className="space-y-5 px-5 py-5 md:px-8 md:py-7">
            <section
                className="rounded-[24px] border border-[var(--line)] bg-white p-4 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-6">
                <div className="max-w-3xl">
                    <div className="inline-flex rounded-full bg-[var(--accent-soft)] px-3 py-1 text-xs font-semibold text-[var(--accent)]">
                        커뮤니티
                    </div>
                    <h1 className="mt-3 text-[22px] font-black tracking-tight text-slate-950 md:text-[28px]">
                        다녀온 사람의 경험이 가장 정확한 정보
                    </h1>
                    <p className="mt-2 hidden max-w-2xl text-sm leading-6 text-slate-600 md:block">
                        후기, 꿀팁, 자유 글과 요청 글을 각각 나눠서 확인하세요.
                    </p>
                </div>
            </section>

            <section className="rounded-[24px] border border-[var(--line)] bg-white p-3 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-4">
                <div className="flex flex-wrap items-center justify-between gap-3">
                    <div className="flex flex-wrap items-center gap-2">
                        <div className="flex flex-wrap items-center gap-1.5">
                            <span className="text-[10px] font-semibold text-slate-500">카테고리</span>
                            <FilterChip active={feedTab === 'posts' && !categoryId} tone="violet" onClick={() => {
                                setFeedTab('posts')
                                setCategoryId(undefined)
                            }}>
                                전체
                            </FilterChip>
                            {categories.map((category: any) => {
                                const key = normalizeCategoryKey(category.name)
                                if (key !== 'review' && key !== 'free' && key !== 'tip') return null
                                return (
                                    <FilterChip
                                        key={category.id}
                                        active={feedTab === 'posts' && categoryId === category.id}
                                        tone={categoryTone(category.name)}
                                        onClick={() => {
                                            setFeedTab('posts')
                                            setCategoryId(category.id)
                                        }}
                                    >
                                        {category.name}
                                    </FilterChip>
                                )
                            })}
                            <span className="ml-1 text-[10px] font-semibold text-slate-500">요청</span>
                            <FilterChip active={feedTab === 'requests' && requestKind === 'event'} tone="pink" onClick={() => {
                                setFeedTab('requests')
                                setRequestKind('event')
                            }}>
                                이벤트
                            </FilterChip>
                            {canViewOperationRequests && (
                                <FilterChip active={feedTab === 'requests' && requestKind === 'operation'} tone="rose" onClick={() => {
                                    setFeedTab('requests')
                                    setRequestKind('operation')
                                }}>
                                    운영
                                </FilterChip>
                            )}
                        </div>
                    </div>

                    <div className="flex flex-wrap items-center gap-1.5">
                        {feedTab === 'posts' && (
                            <>
                                <span className="text-[10px] font-semibold text-slate-500">정렬</span>
                                <FilterChip active={sort === 'latest'} tone="sky" onClick={() => setSort('latest')}>
                                    최신글
                                </FilterChip>
                                <FilterChip active={sort === 'popular'} tone="sky" onClick={() => setSort('popular')}>
                                    인기글
                                </FilterChip>
                            </>
                        )}
                    </div>
                </div>
            </section>

            {feedTab === 'posts' ? (
                <>
                    <section className="space-y-3 rounded-[24px] border border-[var(--line)] bg-white p-4 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-5">
                        <div className="flex items-center justify-between gap-3">
                            <div>
                                <h2 className="text-[18px] font-black tracking-tight text-slate-950">
                                    {sort === 'popular' ? '오늘 인기글' : '최신글'}
                                </h2>
                                <p className="mt-1 hidden text-xs text-slate-500 md:block">
                                    {sort === 'popular'
                                        ? '오늘 올라온 글 가운데 좋아요가 많은 글을 보고 있어요.'
                                        : `${categoryId ? `${categoryNameById.get(categoryId) ?? '선택한'} 카테고리` : '모든 카테고리'} 글을 보고 있어요.`}
                                </p>
                            </div>
                            <Link
                                to="/community/new"
                                className="rounded-full bg-[var(--accent-soft)] px-4 py-2 text-xs font-semibold text-[var(--accent)] hover:bg-white"
                            >
                                글쓰기
                            </Link>
                        </div>

                        <div className="space-y-3">
                            {posts.map((post: any) => (
                                <PostCard
                                    key={post.id}
                                    post={post}
                                    categoryLabel={categoryNameById.get(post.categoryId) ?? post.categoryName}
                                />
                            ))}
                        </div>
                    </section>
                </>
            ) : (
                <section className="space-y-4 rounded-[24px] border border-[var(--line)] bg-white p-4 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-5">
                    <div className="flex items-center justify-between gap-3">
                        <div>
                            <h2 className="text-[18px] font-black tracking-tight text-slate-950">요청 글</h2>
                            <p className="mt-1 hidden text-xs text-slate-500 md:block">
                                {canViewAllRequests
                                    ? '관리자와 매니저는 전체 요청을 봅니다.'
                                    : '내가 작성한 요청만 볼 수 있어요.'}
                            </p>
                        </div>
                        {user?.role === 'USER' ? (
                            <div className="flex flex-wrap items-center gap-2">
                                {requestKind === 'event' ? (
                                    <Link
                                        to="/community/new?requestKind=event"
                                        className="rounded-full bg-[var(--accent-soft)] px-4 py-2 text-xs font-semibold text-[var(--accent)] hover:bg-white"
                                    >
                                        이벤트 요청 작성
                                    </Link>
                                ) : (
                                    <Link
                                        to="/community/new?requestKind=operation"
                                        className="rounded-full bg-[var(--accent-soft)] px-4 py-2 text-xs font-semibold text-[var(--accent)] hover:bg-white"
                                    >
                                        운영 요청 작성
                                    </Link>
                                )}
                            </div>
                        ) : (
                            <div className="text-xs text-slate-500">
                                요청 작성은 일반 사용자만 가능합니다.
                            </div>
                        )}
                    </div>

                    <div className="space-y-3">
                        {requestItems.length === 0 ? (
                            <div className="rounded-[20px] border border-dashed border-[var(--line)] bg-slate-50 px-4 py-8 text-center text-sm text-slate-500">
                                아직 요청 글이 없어요.
                            </div>
                        ) : (
                            requestItems.map((request) => {
                                const canModerate = request.kind === 'event' ? canModerateEventRequests : canViewOperationRequests
                                const reason = requestRejectReasons[request.id] ?? ''
                                const actions = canModerate && request.status === 'PENDING' ? (
                                    <div className="space-y-2 border-t border-slate-200 pt-3">
                                        <input
                                            value={reason}
                                            onChange={(e) => setRequestRejectReasons((prev) => ({
                                                ...prev,
                                                [request.id]: e.target.value,
                                            }))}
                                            placeholder="반려 사유"
                                            className="w-full rounded-full border border-[var(--line)] bg-white px-4 py-2 text-xs outline-none"
                                        />
                                        <div className="flex flex-wrap gap-2">
                                            <button
                                                type="button"
                                                onClick={() => {
                                                    if (request.kind === 'event') {
                                                        approveEventMutation.mutate(request.id)
                                                    } else {
                                                        updateOperationStatusMutation.mutate({
                                                            requestId: request.id,
                                                            status: 'IN_PROGRESS',
                                                            reason: reason.trim(),
                                                        })
                                                    }
                                                }}
                                                className="rounded-full bg-[var(--accent-soft)] px-3 py-1.5 text-[11px] font-semibold text-[var(--accent)]"
                                            >
                                                승인
                                            </button>
                                            <button
                                                type="button"
                                                onClick={() => {
                                                    const rejectReason = reason.trim()
                                                    if (!rejectReason) return
                                                    if (request.kind === 'event') {
                                                        rejectEventMutation.mutate({requestId: request.id, reason: rejectReason})
                                                    } else {
                                                        updateOperationStatusMutation.mutate({
                                                            requestId: request.id,
                                                            status: 'REJECTED',
                                                            reason: rejectReason,
                                                        })
                                                    }
                                                }}
                                                className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-[11px] font-semibold text-slate-600"
                                            >
                                                거절
                                            </button>
                                        </div>
                                    </div>
                                ) : null

                                return (
                                    <RequestCard
                                        key={`${request.kind}-${request.id}`}
                                        kindLabel={request.kind === 'event' ? '이벤트' : '운영'}
                                        kindClassName={request.kind === 'event'
                                            ? 'bg-pink-100 text-pink-700'
                                            : 'bg-rose-100 text-rose-700'}
                                        metaLabel={request.metaLabel}
                                        title={request.title}
                                        body={request.body}
                                        authorNickname={request.authorNickname}
                                        createdAt={request.createdAt}
                                        status={request.status}
                                        statusLabel={requestStatusLabel(request.status)}
                                        statusClassName={request.statusClassName}
                                        rejectReason={request.rejectReason}
                                        actions={actions}
                                    />
                                )
                            })
                        )}
                    </div>
                </section>
            )}
        </div>
    )
}

function FilterChip({
    active,
    tone = 'violet',
    onClick,
    children,
}: {
    active?: boolean
    tone?: 'violet' | 'sky' | 'emerald' | 'rose' | 'pink' | 'slate'
    onClick: () => void
    children: ReactNode
}) {
    const toneClass = {
        violet: 'border-[var(--accent)] bg-[var(--accent-soft)] text-[var(--accent)]',
        sky: 'border-sky-200 bg-sky-50 text-sky-700',
        emerald: 'border-emerald-200 bg-emerald-50 text-emerald-700',
        rose: 'border-rose-200 bg-rose-50 text-rose-700',
        pink: 'border-pink-200 bg-pink-50 text-pink-700',
        slate: 'border-slate-200 bg-slate-50 text-slate-700',
    }[tone]

    return (
        <button
            onClick={onClick}
            className={`rounded-full border px-2 py-1 text-[10px] font-semibold transition-colors md:px-2.5 md:py-1.5 md:text-[11px] ${
                active ? toneClass : 'border-[var(--line)] bg-white text-slate-600 hover:bg-slate-50'
            }`}
        >
            {children}
        </button>
    )
}

function categoryTone(name: string) {
    return (
        {
            review: 'violet',
            tip: 'sky',
            free: 'emerald',
            request: 'rose',
        }[normalizeCategoryKey(name)] ?? 'violet'
    ) as 'violet' | 'sky' | 'emerald' | 'rose' | 'slate'
}

function normalizeCategoryKey(name: string) {
    const normalized = String(name ?? '').trim().toLowerCase()
    if (normalized === '후기' || normalized === 'review') return 'review'
    if (normalized === '꿀팁' || normalized === 'tip') return 'tip'
    if (normalized === '자유' || normalized === 'free') return 'free'
    if (normalized === '요청' || normalized === 'request') return 'request'
    return normalized
}

function requestStatusLabel(status?: string | null) {
    const normalized = String(status ?? '').toUpperCase()
    if (normalized === 'PENDING') return '대기중'
    if (normalized === 'APPROVED') return '승인됨'
    if (normalized === 'REJECTED') return '거절됨'
    if (normalized === 'CANCELED' || normalized === 'CANCELLED') return '취소됨'
    if (normalized === 'IN_PROGRESS') return '처리중'
    if (normalized === 'RESOLVED') return '완료'
    return normalized || '상태'
}

function requestStatusClass(status?: string | null) {
    const normalized = String(status ?? '').toUpperCase()
    if (normalized === 'PENDING') return 'bg-amber-100 text-amber-700'
    if (normalized === 'APPROVED' || normalized === 'RESOLVED') return 'bg-emerald-100 text-emerald-700'
    if (normalized === 'REJECTED') return 'bg-rose-100 text-rose-700'
    if (normalized === 'IN_PROGRESS') return 'bg-sky-100 text-sky-700'
    if (normalized === 'CANCELED' || normalized === 'CANCELLED') return 'bg-slate-100 text-slate-600'
    return 'bg-slate-100 text-slate-600'
}
