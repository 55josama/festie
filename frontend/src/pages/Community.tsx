import { type ReactNode, useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getCategories, getPosts } from '../api/community'
import { approveEventRequest, rejectEventRequest, updateOperationRequestStatus } from '../api/admin'
import { deleteEventRequest, deleteOperationRequest, getEventRequests, getMyEventRequests, getMyOperationRequests, getOperationRequests } from '../api/requests'
import { deleteEventRequest as adminDeleteEventRequest } from '../api/admin'
import PostCard from '../components/PostCard'
import RequestCard from '../components/RequestCard'
import { useAuthStore } from '../store/authStore'
import type { EventRequestItem, OperationRequestItem } from '../types/admin'

type FeedTab = 'posts' | 'requests'
type RequestKind = 'event' | 'operation'
type RequestStatusFilter = 'all' | 'PENDING' | 'APPROVED' | 'IN_PROGRESS' | 'RESOLVED' | 'REJECTED'
type RequestCategoryFilter = 'all' | string
type PostScope = 'all' | 'mine'
type PopularRange = 'today' | 'week' | 'month'

type RequestFeedItem = {
    id: string
    requesterId: string
    kind: 'event' | 'operation'
    kindLabel: string
    metaLabel: string
    categoryId?: string
    categoryLabel?: string | null
    title: string
    body: string
    authorNickname?: string | null
    createdAt?: string | null
    createdEventId?: string | null
    status: string
    rejectReason?: string | null
    statusClassName: string
    sortAt: number
    sortIndex: number
}

export default function Community() {
    const { user } = useAuthStore()
    const navigate = useNavigate()
    const [searchParams, setSearchParams] = useSearchParams()
    const queryClient = useQueryClient()
    const [feedTab, setFeedTab] = useState<FeedTab>(() => searchParams.get('tab') === 'requests' ? 'requests' : 'posts')
    const [requestKind, setRequestKind] = useState<RequestKind>(() => searchParams.get('requestKind') === 'operation' ? 'operation' : 'event')
    const [categoryId, setCategoryId] = useState<string | undefined>()
    const [sort, setSort] = useState<'latest' | 'popular'>('latest')
    const [popularRange, setPopularRange] = useState<PopularRange>('today')
    const [postScope, setPostScope] = useState<PostScope>(() => searchParams.get('scope') === 'mine' ? 'mine' : 'all')
    const [requestStatusFilter, setRequestStatusFilter] = useState<RequestStatusFilter>('all')
    const [requestCategoryFilter, setRequestCategoryFilter] = useState<RequestCategoryFilter>('all')
    const [requestRejectReasons, setRequestRejectReasons] = useState<Record<string, string>>({})
    const [operationRejectReasons, setOperationRejectReasons] = useState<Record<string, string>>({})
    const [selectedRequest, setSelectedRequest] = useState<RequestFeedItem | null>(null)
    const isAdmin = !!user && /ADMIN/.test(user.role)
    const isManager = !!user && /_MANAGER$/.test(user.role)
    const canViewRequestTabs = !!user && (user.role === 'USER' || isAdmin || isManager)
    const canViewAllRequests = isAdmin || isManager
    const canModerateEventRequests = isAdmin || isManager

    const handleCreatePostClick = () => {
        if (!user) {
            window.alert('로그인 후 글을 작성해 주세요.')
            navigate('/login')
            return
        }
        navigate('/community/new')
    }

    useEffect(() => {
        const nextFeedTab = searchParams.get('tab') === 'requests' ? 'requests' : 'posts'
        const nextRequestKind = searchParams.get('requestKind') === 'operation' ? 'operation' : 'event'
        const nextPostScope = user && searchParams.get('scope') === 'mine' ? 'mine' : 'all'
        setFeedTab(nextFeedTab)
        setRequestKind(nextRequestKind)
        setPostScope(nextPostScope)
    }, [searchParams, user])

    useEffect(() => {
        if (canViewRequestTabs) return
        if (feedTab !== 'requests') return

        setFeedTab('posts')
        const next = new URLSearchParams(searchParams)
        next.set('tab', 'posts')
        next.delete('requestKind')
        setSearchParams(next, { replace: true })
    }, [canViewRequestTabs, feedTab, searchParams, setSearchParams])

    useEffect(() => {
        if (!isManager || requestKind !== 'operation') return
        setRequestKind('event')
        const next = new URLSearchParams(searchParams)
        next.set('tab', 'requests')
        next.set('requestKind', 'event')
        setSearchParams(next, { replace: true })
    }, [isManager, requestKind, searchParams, setSearchParams])

    const {data: categories = []} = useQuery({
        queryKey: ['categories'],
        queryFn: getCategories,
    })

    const {data: rawPosts = []} = useQuery({
        queryKey: ['posts', categoryId, sort, postScope, user?.userId ?? null],
        queryFn: () => getPosts({
            categoryId,
            sort: sort === 'popular' ? 'likeCount,desc' : 'createdAt,desc',
            size: 100,
            mine: postScope === 'mine' && !!user,
        }),
        enabled: feedTab === 'posts' && (postScope !== 'mine' || !!user),
    })

    const {data: eventRequests = []} = useQuery<EventRequestItem[]>({
        queryKey: ['community', 'event-requests', canViewAllRequests],
        queryFn: () => canViewAllRequests
            ? getEventRequests({size: 100})
            : getMyEventRequests({size: 100}),
        enabled: feedTab === 'requests' && requestKind === 'event',
    })

    const {data: operationRequests = []} = useQuery<OperationRequestItem[]>({
        queryKey: ['community', 'operation-requests', isAdmin],
        queryFn: () => isAdmin
            ? getOperationRequests({size: 100})
            : getMyOperationRequests({size: 100}),
        enabled: feedTab === 'requests' && requestKind === 'operation' && !!user,
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
        mutationFn: ({requestId, status, reason}: { requestId: string; status: string; reason?: string | null }) =>
            updateOperationRequestStatus(requestId, status, reason),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: ['community', 'operation-requests']})
        },
    })

    const deleteOperationMutation = useMutation({
        mutationFn: (requestId: string) => deleteOperationRequest(requestId),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: ['community', 'operation-requests']})
        },
    })

    const deleteEventMutation = useMutation({
        mutationFn: (requestId: string) => isAdmin ? adminDeleteEventRequest(requestId) : deleteEventRequest(requestId),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: ['community', 'event-requests']})
        },
    })

    const categoryNameById = useMemo(() => {
        return new Map(categories.map((category: any) => [category.id, category.name]))
    }, [categories])

    const posts = useMemo(() => {
        if (sort === 'latest') return rawPosts
        const cutoffHours = popularRange === 'today' ? 24 : popularRange === 'week' ? 24 * 7 : 24 * 30
        const cutoff = Date.now() - cutoffHours * 60 * 60 * 1000
        const rangedPosts = [...rawPosts]
            .filter((post: any) => {
                const createdAt = new Date(post.createdAt)
                if (Number.isNaN(createdAt.getTime())) return false
                return createdAt.getTime() >= cutoff
            })
            .sort((a: any, b: any) => (b.likeCount + b.commentCount) - (a.likeCount + a.commentCount))
        return rangedPosts
    }, [rawPosts, popularRange, sort])

    const requestItems = useMemo<RequestFeedItem[]>(() => {
        const eventRequestCards = (eventRequests ?? []).map((request: any, index: number) => ({
            id: request.id,
            requesterId: request.requesterId ?? request.requester_id ?? '',
            kind: 'event' as const,
            kindLabel: '요청',
            metaLabel: '이벤트 요청',
            categoryId: request.categoryId ?? request.category_id ?? '',
            categoryLabel: request.category ?? null,
            title: request.eventName ?? '',
            body: request.description ?? '',
            authorNickname: request.requesterNickname ?? request.requester_nickname ?? null,
            createdAt: request.createdAt ?? null,
            createdEventId: request.createdEventId ?? request.created_event_id ?? null,
            status: request.status ?? request.status_name ?? 'PENDING',
            rejectReason: request.rejectReason ?? request.reject_reason ?? null,
            statusClassName: requestStatusClass(request.status ?? request.status_name),
            sortAt: request.createdAt ? new Date(request.createdAt).getTime() : 0,
            sortIndex: index,
        }))

        const operationRequestCards = (operationRequests ?? []).map((request: any, index: number) => ({
            id: request.id,
            requesterId: request.requesterId ?? request.requester_id ?? '',
            kind: 'operation' as const,
            kindLabel: '요청',
            metaLabel: '운영 요청',
            title: request.title ?? '운영 요청',
            body: request.content ?? '',
            authorNickname: request.requesterNickname ?? request.requester_nickname ?? null,
            createdAt: null,
            status: request.status ?? request.status_name ?? 'PENDING',
            rejectReason: request.adminMemo ?? request.admin_memo ?? null,
            statusClassName: requestStatusClass(request.status ?? request.status_name),
            sortAt: 0,
            sortIndex: index,
        }))

        const combined = [...eventRequestCards, ...operationRequestCards]
        const filtered = requestKind === 'event'
            ? combined.filter((request) => request.kind === 'event')
            : combined.filter((request) => request.kind === 'operation')
        return filtered.sort((left, right) => {
            if (right.sortAt !== left.sortAt) return right.sortAt - left.sortAt
            return left.sortIndex - right.sortIndex
        })
    }, [eventRequests, operationRequests, requestKind])

    const eventRequestCategories = useMemo(() => {
        const seen = new Map<string, string>()
        requestItems
            .filter((request) => request.kind === 'event')
            .forEach((request) => {
                if (!request.categoryId) return
                if (!seen.has(request.categoryId)) {
                    seen.set(request.categoryId, request.categoryLabel ?? request.categoryId)
                }
            })
        return Array.from(seen.entries()).map(([id, name]) => ({ id, name }))
    }, [requestItems])

    const visibleRequestItems = useMemo(() => {
        return requestItems.filter((request) => {
            const matchesStatus = requestStatusFilter === 'all'
                || matchesRequestStatusFilter(request.kind, request.status, requestStatusFilter)
            const matchesCategory = requestKind !== 'event'
                || requestCategoryFilter === 'all'
                || request.categoryId === requestCategoryFilter
            return matchesStatus && matchesCategory
        })
    }, [requestCategoryFilter, requestItems, requestKind, requestStatusFilter])

    const requestStatusOptions = useMemo(() => {
        return requestKind === 'event'
            ? [
                { value: 'all' as const, label: '전체', tone: 'slate' as const },
                { value: 'PENDING' as const, label: '대기중', tone: 'amber' as const },
                { value: 'APPROVED' as const, label: '승인됨', tone: 'emerald' as const },
                { value: 'REJECTED' as const, label: '거절됨', tone: 'rose' as const },
            ]
            : [
                { value: 'all' as const, label: '전체', tone: 'slate' as const },
                { value: 'PENDING' as const, label: '대기중', tone: 'amber' as const },
                { value: 'IN_PROGRESS' as const, label: '처리중', tone: 'sky' as const },
                { value: 'RESOLVED' as const, label: '완료', tone: 'emerald' as const },
                { value: 'REJECTED' as const, label: '거절됨', tone: 'rose' as const },
            ]
    }, [requestKind])

    useEffect(() => {
        setRequestStatusFilter('all')
        setRequestCategoryFilter('all')
    }, [requestKind])

    useEffect(() => {
        if (postScope !== 'mine' || user) return
        setPostScope('all')
        const next = new URLSearchParams(searchParams)
        next.delete('scope')
        setSearchParams(next, { replace: true })
    }, [postScope, searchParams, setSearchParams, user])

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
                            {canViewRequestTabs && (
                                <>
                                    <span className="ml-1 text-[10px] font-semibold text-slate-500">요청</span>
                                    <FilterChip active={feedTab === 'requests' && requestKind === 'event'} tone="pink" onClick={() => {
                                        setFeedTab('requests')
                                        setRequestKind('event')
                                    }}>
                                        이벤트
                                    </FilterChip>
                                    <FilterChip active={feedTab === 'requests' && requestKind === 'operation'} tone="rose" onClick={() => {
                                        setFeedTab('requests')
                                        setRequestKind('operation')
                                    }}>
                                        운영
                                    </FilterChip>
                                </>
                            )}
                        </div>
                    </div>

                    <div className="flex flex-wrap items-center gap-1.5">
                        {feedTab === 'posts' && (
                            <>
                                <span className="text-[10px] font-semibold text-slate-500">조회</span>
                                <FilterChip active={postScope === 'all'} tone="violet" onClick={() => {
                                    setPostScope('all')
                                    const next = new URLSearchParams(searchParams)
                                    next.delete('scope')
                                    setSearchParams(next, { replace: true })
                                }}>
                                    전체
                                </FilterChip>
                                {!!user && (
                                    <FilterChip active={postScope === 'mine'} tone="violet" onClick={() => {
                                        setPostScope('mine')
                                        const next = new URLSearchParams(searchParams)
                                        next.set('scope', 'mine')
                                        next.set('tab', 'posts')
                                        setSearchParams(next, { replace: true })
                                    }}>
                                        내 글
                                    </FilterChip>
                                )}
                                <span className="text-[10px] font-semibold text-slate-500">정렬</span>
                                <FilterChip active={sort === 'latest'} tone="sky" onClick={() => setSort('latest')}>
                                    최신글
                                </FilterChip>
                                <FilterChip active={sort === 'popular'} tone="sky" onClick={() => setSort('popular')}>
                                    인기글
                                </FilterChip>
                                {sort === 'popular' && (
                                    <select
                                        value={popularRange}
                                        onChange={(e) => setPopularRange(e.target.value as PopularRange)}
                                        className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-[11px] font-semibold text-slate-600 outline-none"
                                    >
                                        <option value="today">오늘</option>
                                        <option value="week">이번주</option>
                                        <option value="month">이번달</option>
                                    </select>
                                )}
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
                                    {sort === 'popular'
                                        ? popularRange === 'today'
                                            ? '오늘 인기글'
                                            : popularRange === 'week'
                                                ? '이번주 인기글'
                                                : '이번달 인기글'
                                        : '최신글'}
                                </h2>
                                <p className="mt-1 hidden text-xs text-slate-500 md:block">
                                    {sort === 'popular'
                                        ? popularRange === 'today'
                                            ? '오늘 올라온 글 가운데 좋아요와 댓글이 많은 글을 보고 있어요.'
                                            : popularRange === 'week'
                                                ? '이번주에 올라온 글 가운데 좋아요와 댓글이 많은 글을 보고 있어요.'
                                                : '이번달에 올라온 글 가운데 좋아요와 댓글이 많은 글을 보고 있어요.'
                                        : `${categoryId ? `${categoryNameById.get(categoryId) ?? '선택한'} 카테고리` : '모든 카테고리'} 글을 보고 있어요.`}
                                </p>
                            </div>
                            <button
                                type="button"
                                onClick={handleCreatePostClick}
                                className="rounded-full bg-[var(--accent-soft)] px-4 py-2 text-xs font-semibold text-[var(--accent)] hover:bg-white"
                            >
                                글쓰기
                            </button>
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

                    <div className="flex flex-wrap items-center gap-2 rounded-[18px] border border-[var(--line)] bg-slate-50 px-3 py-3">
                        <span className="text-[10px] font-semibold text-slate-500">상태</span>
                        {requestStatusOptions.map((option) => (
                            <FilterChip
                                key={option.value}
                                active={requestStatusFilter === option.value}
                                tone={option.tone}
                                onClick={() => setRequestStatusFilter(option.value)}
                            >
                                {option.label}
                            </FilterChip>
                        ))}
                    </div>

                    {requestKind === 'event' && (
                        <div className="flex flex-wrap items-center gap-2 rounded-[18px] border border-[var(--line)] bg-slate-50 px-3 py-3">
                            <span className="text-[10px] font-semibold text-slate-500">카테고리</span>
                            <FilterChip
                                active={requestCategoryFilter === 'all'}
                                tone="violet"
                                onClick={() => setRequestCategoryFilter('all')}
                            >
                                전체
                            </FilterChip>
                            {eventRequestCategories.map((category: any) => (
                                <FilterChip
                                    key={category.id}
                                    active={requestCategoryFilter === category.id}
                                    tone="pink"
                                    onClick={() => setRequestCategoryFilter(category.id)}
                                >
                                    {category.name}
                                </FilterChip>
                            ))}
                        </div>
                    )}

                    <div className="space-y-3">
                        {visibleRequestItems.length === 0 ? (
                            <div className="rounded-[20px] border border-dashed border-[var(--line)] bg-slate-50 px-4 py-8 text-center text-sm text-slate-500">
                                아직 요청 글이 없어요.
                            </div>
                        ) : (
                            visibleRequestItems.map((request) => {
                            const reason = requestRejectReasons[request.id] ?? ''
                            const operationReason = operationRejectReasons[request.id] ?? ''
                            const canModerateOperation = isAdmin
                            const isOperationOwner = !!user && request.requesterId === user.userId
                            const isEventOwner = !!user && request.requesterId === user.userId
                            const canDeleteEventRequest = request.kind === 'event' && (isAdmin || isEventOwner)
                            const canEditOperationRequest = request.kind === 'operation' && (isAdmin || (isOperationOwner && request.status === 'PENDING'))
                            const canDeleteOperationRequest = request.kind === 'operation' && (isAdmin || isOperationOwner)
                            const operationActions = (() => {
                                    const canApproveOperation = canModerateOperation && request.status === 'PENDING'
                                    const canResolveOperation = canModerateOperation && request.status === 'IN_PROGRESS'
                                    const canRejectOperation = canModerateOperation && request.status === 'PENDING'
                                    const moderationBlock = canApproveOperation || canResolveOperation || canRejectOperation ? (
                                        <div className="space-y-2.5">
                                            <input
                                                value={operationReason}
                                                onChange={(e) => setOperationRejectReasons((prev) => ({
                                                    ...prev,
                                                    [request.id]: e.target.value,
                                                }))}
                                                placeholder="관리자 코멘트"
                                                className="w-full rounded-full border border-[var(--line)] bg-white px-4 py-2 text-sm outline-none"
                                            />
                                            <div className="flex items-center gap-3 pt-1.5">
                                                {canApproveOperation && (
                                                    <button
                                                        type="button"
                                                        onClick={() => {
                                                            updateOperationStatusMutation.mutate({
                                                                requestId: request.id,
                                                                status: 'IN_PROGRESS',
                                                                reason: operationReason.trim() || null,
                                                            })
                                                        }}
                                                        className="flex-1 rounded-full bg-[var(--accent-soft)] px-4 py-2.5 text-sm font-semibold text-[var(--accent)]"
                                                    >
                                                        승인
                                                    </button>
                                                )}
                                                {canResolveOperation && (
                                                    <button
                                                        type="button"
                                                        onClick={() => {
                                                            updateOperationStatusMutation.mutate({
                                                                requestId: request.id,
                                                                status: 'RESOLVED',
                                                                reason: operationReason.trim() || null,
                                                            })
                                                        }}
                                                        className="flex-1 rounded-full border border-emerald-200 bg-emerald-50 px-4 py-2.5 text-sm font-semibold text-emerald-700"
                                                    >
                                                        처리완료
                                                    </button>
                                                )}
                                                {canRejectOperation && (
                                                    <button
                                                        type="button"
                                                        onClick={() => {
                                                            const memo = operationReason.trim()
                                                            if (!memo) {
                                                                window.alert('관리자 코멘트를 입력해 주세요.')
                                                                return
                                                            }
                                                            updateOperationStatusMutation.mutate({
                                                                requestId: request.id,
                                                                status: 'REJECTED',
                                                                reason: memo,
                                                            })
                                                        }}
                                                        className="flex-1 rounded-full bg-rose-500 px-4 py-2.5 text-sm font-semibold text-white"
                                                    >
                                                        거절
                                                    </button>
                                                )}
                                            </div>
                                        </div>
                                    ) : null

                                    if (!moderationBlock) return null
                                    return moderationBlock
                                })()
                                const eventActions = request.kind === 'event' ? (
                                    <div className="space-y-2">
                                        {canModerateEventRequests && request.status === 'PENDING' && (
                                            <div className="space-y-2">
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
                                                        onClick={() => approveEventMutation.mutate(request.id)}
                                                        className="rounded-full bg-[var(--accent-soft)] px-3 py-1.5 text-[11px] font-semibold text-[var(--accent)]"
                                                    >
                                                        승인
                                                    </button>
                                                    <button
                                                        type="button"
                                                        onClick={() => {
                                                            const rejectReason = reason.trim()
                                                            if (!rejectReason) return
                                                            rejectEventMutation.mutate({requestId: request.id, reason: rejectReason})
                                                        }}
                                                        className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-[11px] font-semibold text-slate-600"
                                                    >
                                                        거절
                                                    </button>
                                                </div>
                                            </div>
                                        )}
                                        {canModerateEventRequests && request.status === 'APPROVED' && !request.createdEventId && (
                                            <Link
                                                to={`/admin?tab=manage&requestId=${request.id}`}
                                                className="inline-flex w-full items-center justify-center rounded-full border border-violet-300 bg-violet-50 px-5 py-3 text-sm font-semibold text-violet-700 shadow-[0_0_0_1px_rgba(167,139,250,0.08)] transition-colors hover:bg-violet-100"
                                            >
                                                행사 생성
                                            </Link>
                                        )}
                                        {request.createdEventId && (
                                            <Link
                                                to={`/events/${request.createdEventId}`}
                                                className="inline-flex w-full items-center justify-center rounded-full border border-[var(--line)] bg-white px-3 py-2 text-sm font-semibold text-slate-700"
                                            >
                                                생성된 행사 보기
                                            </Link>
                                        )}
                                    </div>
                                ) : null
                                const actions = request.kind === 'event'
                                    ? eventActions
                                    : operationActions
                                const statusActions = request.kind === 'event'
                                    ? (canDeleteEventRequest ? (
                                        <button
                                            type="button"
                                            onClick={() => {
                                                if (window.confirm('이벤트 요청을 삭제할까요?')) {
                                                    deleteEventMutation.mutate(request.id)
                                                }
                                            }}
                                            className="rounded-full border border-rose-200 bg-rose-50 px-3 py-1 text-[10px] font-semibold text-rose-700"
                                            disabled={deleteEventMutation.isPending}
                                        >
                                            삭제
                                        </button>
                                    ) : null)
                                    : (
                                        <div className="flex items-center gap-2">
                                            {canEditOperationRequest && (
                                                <Link
                                                    to={`/community/new?requestKind=operation&requestId=${request.id}`}
                                                    className="inline-flex min-w-[64px] items-center justify-center rounded-full border border-violet-200 bg-violet-50 px-3 py-1.5 text-[11px] font-semibold text-violet-700"
                                                >
                                                    수정
                                                </Link>
                                            )}
                                            {canDeleteOperationRequest && (
                                                <button
                                                    type="button"
                                                    onClick={() => {
                                                        if (window.confirm('운영 요청을 삭제할까요?')) {
                                                            deleteOperationMutation.mutate(request.id)
                                                        }
                                                    }}
                                                    className="inline-flex min-w-[64px] items-center justify-center rounded-full border border-rose-200 bg-rose-50 px-3 py-1.5 text-[11px] font-semibold text-rose-700"
                                                    disabled={deleteOperationMutation.isPending}
                                                >
                                                    삭제
                                                </button>
                                            )}
                                        </div>
                                    )

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
                                        statusActions={statusActions}
                                        actions={actions}
                                        onClick={request.kind === 'event' ? () => setSelectedRequest(request) : undefined}
                                    />
                                )
                            })
                        )}
                    </div>
                </section>
            )}
            {selectedRequest && selectedRequest.kind === 'event' && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/50 px-4 py-6" onClick={() => setSelectedRequest(null)}>
                    <div
                        className="w-full max-w-2xl rounded-[28px] bg-white p-5 shadow-[0_20px_60px_rgba(15,23,42,0.18)]"
                        onClick={(e) => e.stopPropagation()}
                    >
                        <div className="flex items-start justify-between gap-3">
                            <div>
                                <div className="inline-flex rounded-full bg-[var(--accent-soft)] px-3 py-1 text-[11px] font-semibold text-[var(--accent)]">
                                    요청 상세
                                </div>
                                <h3 className="mt-3 text-[22px] font-black tracking-tight text-slate-950">
                                    {selectedRequest.title || selectedRequest.kindLabel}
                                </h3>
                                <p className="mt-1 text-xs text-slate-500">
                                    이벤트 요청의 상세 정보를 확인할 수 있어요.
                                </p>
                            </div>
                            <button
                                type="button"
                                onClick={() => setSelectedRequest(null)}
                                className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-600"
                            >
                                닫기
                            </button>
                        </div>

                        <div className="mt-5 grid gap-3 md:grid-cols-2">
                            <DetailItem label="카테고리" value={selectedRequest.categoryLabel ?? selectedRequest.metaLabel} />
                            <DetailItem label="작성자" value={selectedRequest.authorNickname ?? selectedRequest.requesterId} />
                            <DetailItem label="요청일" value={selectedRequest.createdAt ?? '-'} />
                            <DetailItem label="상태" value={requestStatusLabel(selectedRequest.status)} />
                        </div>

                        <div className="mt-4 rounded-[20px] border border-[var(--line)] bg-slate-50 p-4">
                            <div className="text-[11px] font-semibold text-slate-500">행사명</div>
                            <div className="mt-1 text-sm font-bold text-slate-950">{selectedRequest.title}</div>
                            <div className="mt-4 text-[11px] font-semibold text-slate-500">설명</div>
                            <div className="mt-1 whitespace-pre-wrap break-words text-sm leading-6 text-slate-700">
                                {selectedRequest.body}
                            </div>
                            {selectedRequest.rejectReason && (
                                <div className={`mt-4 rounded-[16px] border px-3 py-3 text-[12px] leading-5 ${
                                    selectedRequest.status === 'REJECTED'
                                        ? 'border-rose-200 bg-rose-50 text-rose-700'
                                        : 'border-emerald-200 bg-emerald-50 text-emerald-700'
                                }`}>
                                    <div className="font-semibold">{selectedRequest.status === 'REJECTED' ? '반려 사유' : '관리자 메모'}</div>
                                    <div className="mt-1 whitespace-pre-wrap break-words">{selectedRequest.rejectReason}</div>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}

function DetailItem({ label, value }: { label: string; value: string }) {
    return (
        <div className="rounded-[16px] border border-[var(--line)] bg-slate-50 px-3 py-3">
            <div className="text-[11px] font-semibold text-slate-500">{label}</div>
            <div className="mt-1 text-sm font-semibold text-slate-900">{value}</div>
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
    tone?: 'violet' | 'sky' | 'emerald' | 'rose' | 'pink' | 'slate' | 'amber'
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
        amber: 'border-amber-200 bg-amber-50 text-amber-700',
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
    if (normalized === 'IN_PROGRESS') return 'bg-emerald-100 text-emerald-700'
    if (normalized === 'CANCELED' || normalized === 'CANCELLED') return 'bg-slate-100 text-slate-600'
    return 'bg-slate-100 text-slate-600'
}

function matchesRequestStatusFilter(
    kind: RequestKind,
    status?: string | null,
    filter?: RequestStatusFilter,
) {
    const normalized = String(status ?? '').toUpperCase()
    const nextFilter = filter ?? 'all'
    if (nextFilter === 'all') return true
    if (kind === 'event') {
        if (nextFilter === 'PENDING') return normalized === 'PENDING'
        if (nextFilter === 'APPROVED') return normalized === 'APPROVED'
        if (nextFilter === 'REJECTED') return normalized === 'REJECTED'
        return true
    }
    if (nextFilter === 'PENDING') return normalized === 'PENDING'
    if (nextFilter === 'IN_PROGRESS') return normalized === 'IN_PROGRESS' || normalized === 'APPROVED'
    if (nextFilter === 'RESOLVED') return normalized === 'RESOLVED'
    if (nextFilter === 'REJECTED') return normalized === 'REJECTED'
    return true
}
