import {type ReactNode, useEffect, useMemo, useRef, useState} from 'react'
import {Link, useNavigate, useSearchParams} from 'react-router-dom'
import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query'
import {
  approveEventRequest,
  getAdminChatRooms,
  forceChatRoomStatus,
  getEventRequests,
  getOperationRequests,
  getReports,
  rejectEventRequest,
  updateOperationRequestStatus,
  updateReportStatus,
} from '../api/admin'
import {createEvent} from '../api/events'
import {getAdminChatMessages, updateAdminChatMessageStatus} from '../api/chat'
import {
  createCommunityCategory,
  createEventCategory,
  deleteCommunityCategory,
  deleteEventCategory,
  getCommunityCategories,
  getEventCategories,
  updateCommunityCategory,
  updateEventCategory,
} from '../api/categories'
import {useAuthStore} from '../store/authStore'
import {formatDateTime} from '../lib/format'
import {searchAddressWithKakao} from '../lib/kakao'
import type {AdminMessageItem, OperationRequestItem} from '../types/admin'

const REPORT_STATUS_FILTERS = ['ALL', 'AUTO_BLINDED', 'RESOLVED', 'REJECTED'] as const
const REQUEST_STATUS_FILTERS = ['ALL', 'PENDING', 'APPROVED', 'REJECTED', 'CANCELED'] as const
const OPERATION_STATUS_FILTERS = ['ALL', 'PENDING', 'IN_PROGRESS', 'RESOLVED', 'REJECTED'] as const
const CHAT_STATUS_FILTERS = ['ALL', 'SCHEDULED', 'OPEN', 'CLOSED'] as const
const ADMIN_TABS = ['requests', 'reports', 'chat'] as const
const EVENT_CATEGORY_SCOPE: Record<string, string[]> = {
    ADMIN: [],
    CONCERT_MANAGER: ['\uCF58\uC11C\uD2B8'],
    FESTIVAL_MANAGER: ['\uCD95\uC81C'],
    FANMEETING_MANAGER: ['\uD32C\uBBF8\uD305'],
    POPUP_MANAGER: ['\uD31D\uC5C5\uC2A4\uD1A0\uC5B4'],
}

type AdminTab = (typeof ADMIN_TABS)[number]

interface AdminMessagePage {
    content: AdminMessageItem[]
    totalElements: number
    totalPages: number
    size: number
    number: number
}

const EMPTY_ADMIN_MESSAGE_PAGE: AdminMessagePage = {
    content: [],
    totalElements: 0,
    totalPages: 0,
    size: 0,
    number: 0,
}

export default function Admin() {
    const queryClient = useQueryClient()
    const navigate = useNavigate()
    const [searchParams, setSearchParams] = useSearchParams()
    const {user} = useAuthStore()
    const activeTab = normalizeAdminTab(searchParams.get('tab'))
    const [reportStatus, setReportStatus] = useState<(typeof REPORT_STATUS_FILTERS)[number]>('ALL')
    const [requestStatus, setRequestStatus] = useState<(typeof REQUEST_STATUS_FILTERS)[number]>('ALL')
    const [operationStatus, setOperationStatus] = useState<(typeof OPERATION_STATUS_FILTERS)[number]>('ALL')
    const [chatStatus, setChatStatus] = useState<(typeof CHAT_STATUS_FILTERS)[number]>('ALL')
    const [messageStatus, setMessageStatus] = useState<'ALL' | 'ACTIVE' | 'BLINDED'>('ALL')
    const [messagePage, setMessagePage] = useState(0)
    const [rejectReasons, setRejectReasons] = useState<Record<string, string>>({})
    const [reportForms, setReportForms] = useState<Record<string, { status: string; operatorMemo: string }>>({})
    const [operationForms, setOperationForms] = useState<Record<string, { status: string; adminMemo: string }>>({})
    const [requestPanels, setRequestPanels] = useState<Record<string, boolean>>({})
    const [requestDrafts, setRequestDrafts] = useState<Record<string, EventDraft>>({})
    const [generalCreateOpen, setGeneralCreateOpen] = useState(false)
    const [generalDraft, setGeneralDraft] = useState<EventDraft>(createBlankEventDraft())
    const [addressLookupLoading, setAddressLookupLoading] = useState(false)
    const [eventCategoryDraft, setEventCategoryDraft] = useState('')
    const [communityCategoryDraft, setCommunityCategoryDraft] = useState('')
    const [categoryDrafts, setCategoryDrafts] = useState<Record<string, string>>({})
    const [categoryErrors, setCategoryErrors] = useState<{ event: string; community: string }>({
        event: '',
        community: '',
    })
    const categoriesSectionRef = useRef<HTMLDivElement | null>(null)

    useEffect(() => {
        const panel = searchParams.get('panel')
        if (panel === 'general') {
            setGeneralCreateOpen(true)
        }
        if (panel === 'categories') {
            window.requestAnimationFrame(() => {
                categoriesSectionRef.current?.scrollIntoView({behavior: 'smooth', block: 'start'})
            })
        }
    }, [searchParams])

    useEffect(() => {
        if (!searchParams.get('tab')) {
            const next = new URLSearchParams(searchParams)
            next.set('tab', 'requests')
            setSearchParams(next, {replace: true})
        }
    }, [searchParams, setSearchParams])

    const {data: eventCategories = []} = useQuery({
        queryKey: ['admin', 'event-categories'],
        queryFn: getEventCategories,
    })

    const {data: communityCategories = []} = useQuery({
        queryKey: ['admin', 'community-categories'],
        queryFn: getCommunityCategories,
    })

    const normalizedRole = normalizeRole(user?.role)
    const isAdmin = normalizedRole === 'ADMIN'
    const isManager = normalizedRole === 'MANAGER' || normalizedRole.endsWith('_MANAGER')
    const canViewEventRequests = isAdmin || isManager
    const canCreateCategories = normalizedRole === 'ADMIN'
    const canViewOperationRequests = normalizedRole === 'ADMIN'

    const {data: eventRequests = []} = useQuery({
        queryKey: ['admin', 'event-requests', requestStatus],
        queryFn: () => getEventRequests({size: 12, status: requestStatus === 'ALL' ? undefined : requestStatus}),
    })

    const {data: operationRequests = []} = useQuery({
        queryKey: ['admin', 'operation-requests', operationStatus],
        queryFn: () => getOperationRequests({
            size: 12,
            status: operationStatus === 'ALL' ? undefined : operationStatus
        }),
        enabled: canViewOperationRequests,
    })

    const {data: reports = []} = useQuery({
        queryKey: ['admin', 'reports', reportStatus],
        queryFn: () => getReports({size: 12, status: reportStatus === 'ALL' ? undefined : reportStatus}),
    })

    const eventCategoryMap = useMemo(
        () => new Map((eventCategories as any[]).map((category) => [category.name, category.id])),
        [eventCategories],
    )

    const managedEventCategories = useMemo(() => {
        if (normalizedRole === 'ADMIN') return []
        if (!normalizedRole) return []
        return EVENT_CATEGORY_SCOPE[normalizedRole] ?? []
    }, [normalizedRole])

    const managedChatCategories = useMemo(
        () => new Set(managedEventCategories.map((name) => EVENT_CATEGORY_TO_CHAT[normalizeEventCategoryKey(name)] ?? name.toUpperCase())),
        [managedEventCategories],
    )
    const managedMessageCategory = useMemo(() => {
        if (isAdmin) return undefined
        return managedChatCategories.values().next().value as string | undefined
    }, [isAdmin, managedChatCategories])

    const {data: chatRooms = []} = useQuery({
        queryKey: ['admin', 'chat-rooms'],
        queryFn: getAdminChatRooms,
    })

    const {
        data: adminMessagePage = EMPTY_ADMIN_MESSAGE_PAGE,
        refetch: refetchAdminMessages,
    } = useQuery({
        queryKey: ['admin', 'messages', messageStatus, messagePage, managedMessageCategory ?? 'ALL'],
        queryFn: () =>
            getAdminChatMessages({
                page: messagePage,
                size: 8,
                status: messageStatus === 'ALL' ? undefined : messageStatus,
                category: managedMessageCategory,
            }),
    })

    const scopedEventRequests = useMemo(() => {
        if (isAdmin || isManager) return eventRequests
        if (!managedEventCategories.length) return []
        return eventRequests.filter((request: any) => managedEventCategories.includes(request.category))
    }, [eventRequests, managedEventCategories, isAdmin, isManager])

    const scopedChatRooms = useMemo(() => {
        let rooms = isAdmin || isManager ? chatRooms : []
        if (!isAdmin && !isManager && managedChatCategories.size > 0) {
            rooms = rooms.filter((room: any) => managedChatCategories.has(room.category))
        }
        if (chatStatus !== 'ALL') {
            rooms = rooms.filter((room: any) => room.status === chatStatus)
        }
        return rooms
    }, [chatRooms, chatStatus, managedChatCategories, isAdmin, isManager])

    const generalDraftWithCategory = useMemo(() => {
        if (generalDraft.categoryId) return generalDraft
        const firstCategory = (eventCategories as any[])[0]
        return {
            ...generalDraft,
            categoryId: firstCategory?.id ?? '',
        }
    }, [eventCategories, generalDraft])

    const scopedOperationRequests = useMemo(() => {
        if (!canViewOperationRequests) return []
        return operationRequests as OperationRequestItem[]
    }, [canViewOperationRequests, operationRequests])

    const approveMutation = useMutation({
        mutationFn: approveEventRequest,
        onSuccess: () => queryClient.invalidateQueries({queryKey: ['admin', 'event-requests']}),
    })

    const rejectMutation = useMutation({
        mutationFn: ({requestId, rejectReason}: { requestId: string; rejectReason: string }) =>
            rejectEventRequest(requestId, rejectReason),
        onSuccess: () => queryClient.invalidateQueries({queryKey: ['admin', 'event-requests']}),
    })

    const reportMutation = useMutation({
        mutationFn: ({reportId, status, operatorMemo}: { reportId: string; status: string; operatorMemo: string }) =>
            updateReportStatus(reportId, status, operatorMemo),
        onSuccess: () => queryClient.invalidateQueries({queryKey: ['admin', 'reports']}),
    })

    const messageMutation = useMutation({
        mutationFn: ({messageId, status}: { messageId: string; status: 'ACTIVE' | 'BLINDED' }) =>
            updateAdminChatMessageStatus(messageId, status),
        onSuccess: () => queryClient.invalidateQueries({queryKey: ['admin', 'messages']}),
    })

    const createEventMutation = useMutation({
        mutationFn: ({requestId, draft}: { requestId?: string; draft: EventDraft }) =>
            createEvent({
                requestId,
                name: draft.eventName,
                categoryId: draft.categoryId,
                startAt: draft.startAt,
                endAt: draft.endAt,
                place: draft.place,
                region: draft.region || null,
                latitude: toRequiredNumber(draft.latitude),
                longitude: toRequiredNumber(draft.longitude),
                radius: toOptionalNumber(draft.radius),
                minFee: toRequiredNumber(draft.minFee),
                maxFee: toRequiredNumber(draft.maxFee),
                hasTicketing: draft.hasTicketing,
                ticketingOpenAt: normalizeDateTimeInput(draft.ticketingOpenAt),
                ticketingCloseAt: normalizeDateTimeInput(draft.ticketingCloseAt),
                ticketingLink: draft.ticketingLink || null,
                officialLink: draft.officialLink || null,
                performer: draft.performer || null,
                description: draft.description || null,
                img: draft.img,
                schedules: (draft.schedules?.length
                    ? draft.schedules
                    : [{
                        name: draft.eventName || '메인 일정',
                        startAt: draft.startAt,
                        endAt: draft.endAt
                    }]).map((schedule, index) => ({
                    name: schedule.name || `Day ${index + 1}`,
                    startTime: normalizeDateTimeInput(schedule.startAt) ?? draft.startAt,
                    endTime: normalizeDateTimeInput(schedule.endAt) ?? draft.endAt,
                })),
                status: 'SCHEDULED',
            }),
        onSuccess: async (createdEvent) => {
            await queryClient.invalidateQueries({queryKey: ['admin', 'event-requests']})
            await queryClient.invalidateQueries({queryKey: ['events']})
            await queryClient.invalidateQueries({queryKey: ['admin', 'chat-rooms']})
            await queryClient.invalidateQueries({queryKey: ['admin', 'event-categories']})
            navigate(`/events/${createdEvent.id}`)
        },
    })

    const operationRequestMutation = useMutation({
        mutationFn: ({requestId, status, adminMemo}: { requestId: string; status: string; adminMemo: string }) =>
            updateOperationRequestStatus(requestId, status, adminMemo),
        onSuccess: () => queryClient.invalidateQueries({queryKey: ['admin', 'operation-requests']}),
    })

    const chatRoomMutation = useMutation({
        mutationFn: ({chatRoomId, action}: { chatRoomId: string; action: 'FORCE_OPEN' | 'FORCE_CLOSE' }) =>
            forceChatRoomStatus(chatRoomId, action),
        onSuccess: () => queryClient.invalidateQueries({queryKey: ['admin', 'chat-rooms']}),
    })

    const createEventCategoryMutation = useMutation({
        mutationFn: (name: string) => createEventCategory(name),
        onSuccess: async () => {
            setEventCategoryDraft('')
            setCategoryErrors((prev) => ({...prev, event: ''}))
            await queryClient.invalidateQueries({queryKey: ['admin', 'event-categories']})
        },
        onError: (error: any) => {
            setCategoryErrors((prev) => ({
                ...prev,
                event: extractErrorMessage(error, '행사 카테고리를 생성하지 못했어요.'),
            }))
        },
    })

    const updateEventCategoryMutation = useMutation({
        mutationFn: ({categoryId, name}: { categoryId: string; name: string }) => updateEventCategory(categoryId, name),
        onSuccess: async () => {
            setCategoryErrors((prev) => ({...prev, event: ''}))
            await queryClient.invalidateQueries({queryKey: ['admin', 'event-categories']})
        },
        onError: (error: any) => {
            setCategoryErrors((prev) => ({
                ...prev,
                event: extractErrorMessage(error, '행사 카테고리를 수정하지 못했어요.'),
            }))
        },
    })

    const deleteEventCategoryMutation = useMutation({
        mutationFn: (categoryId: string) => deleteEventCategory(categoryId),
        onSuccess: async () => {
            setCategoryErrors((prev) => ({...prev, event: ''}))
            await queryClient.invalidateQueries({queryKey: ['admin', 'event-categories']})
        },
        onError: (error: any) => {
            setCategoryErrors((prev) => ({
                ...prev,
                event: extractErrorMessage(error, '행사 카테고리를 삭제하지 못했어요.'),
            }))
        },
    })

    const createCommunityCategoryMutation = useMutation({
        mutationFn: (name: string) => createCommunityCategory(name),
        onSuccess: async () => {
            setCommunityCategoryDraft('')
            setCategoryErrors((prev) => ({...prev, community: ''}))
            await queryClient.invalidateQueries({queryKey: ['admin', 'community-categories']})
        },
        onError: (error: any) => {
            setCategoryErrors((prev) => ({
                ...prev,
                community: extractErrorMessage(error, '커뮤니티 카테고리를 생성하지 못했어요.'),
            }))
        },
    })

    const updateCommunityCategoryMutation = useMutation({
        mutationFn: ({categoryId, name}: {
            categoryId: string;
            name: string
        }) => updateCommunityCategory(categoryId, name),
        onSuccess: async () => {
            setCategoryErrors((prev) => ({...prev, community: ''}))
            await queryClient.invalidateQueries({queryKey: ['admin', 'community-categories']})
        },
        onError: (error: any) => {
            setCategoryErrors((prev) => ({
                ...prev,
                community: extractErrorMessage(error, '커뮤니티 카테고리를 수정하지 못했어요.'),
            }))
        },
    })

    const deleteCommunityCategoryMutation = useMutation({
        mutationFn: (categoryId: string) => deleteCommunityCategory(categoryId),
        onSuccess: async () => {
            setCategoryErrors((prev) => ({...prev, community: ''}))
            await queryClient.invalidateQueries({queryKey: ['admin', 'community-categories']})
        },
        onError: (error: any) => {
            setCategoryErrors((prev) => ({
                ...prev,
                community: extractErrorMessage(error, '커뮤니티 카테고리를 삭제하지 못했어요.'),
            }))
        },
    })

    const summary = useMemo(() => ({
        eventRequests: scopedEventRequests.length,
        operationRequests: scopedOperationRequests.length,
        reports: reports.length,
        rooms: scopedChatRooms.length,
        messages: adminMessagePage.totalElements,
    }), [adminMessagePage.totalElements, reports.length, scopedChatRooms.length, scopedEventRequests.length, scopedOperationRequests.length])

    const adminMessages = adminMessagePage.content
    const messageTotalPages = Math.max(adminMessagePage.totalPages || 0, 1)

    const lookupPlaceForDraft = async (setDraft: (patch: Partial<EventDraft>) => void) => {
        setAddressLookupLoading(true)
        try {
            const result = await searchAddressWithKakao()
            setDraft({
                place: result.address,
                region: result.region,
                latitude: result.latitude != null ? String(result.latitude) : '',
                longitude: result.longitude != null ? String(result.longitude) : '',
            })
        } finally {
            setAddressLookupLoading(false)
        }
    }

    const attachImageForDraft = async (file: File, setDraft: (patch: Partial<EventDraft>) => void) => {
        const preview = await readFileAsDataUrl(file)
        setDraft({
            img: preview,
            imgFileName: file.name,
        })
    }

    const updateTab = (tab: AdminTab) => {
        const next = new URLSearchParams(searchParams)
        next.set('tab', tab)
        if (tab !== 'requests') {
            next.delete('panel')
        }
        setSearchParams(next, {replace: true})
    }

    const tabs: { value: AdminTab; label: string; description: string }[] = [
        {value: 'requests', label: '요청', description: '행사/운영/카테고리생성'},
        {value: 'reports', label: '신고', description: '블라인드·처리'},
        {value: 'chat', label: '채팅', description: '메시지·방'},
    ]

    return (
        <div className="space-y-6 px-5 py-5 md:px-8 md:py-7">
            {activeTab === 'requests' && (
                <section
                    className="rounded-[28px] bg-slate-950 px-5 py-3.5 text-white shadow-[0_12px_30px_rgba(15,23,42,0.14)] md:px-6 md:py-4">
                    <div className="grid gap-4 lg:grid-cols-[1.1fr_0.9fr] lg:items-start">
                        <div className="space-y-2.5">
                            <div
                                className="inline-flex rounded-full bg-white/10 px-3 py-1 text-xs font-semibold text-slate-100">관리
                            </div>
                            <div className="text-[22px] font-black tracking-tight text-white md:text-[26px]">
                                요청, 신고, 채팅 운영을 관리할 수 있습니다.
                            </div>
                            <div className="rounded-[20px] bg-white/5 p-3 text-xs leading-5 text-slate-300">
                                <div className="text-xs font-semibold text-slate-400">접속 정보</div>
                                <div className="mt-1.5 text-sm leading-6 text-slate-300">
                                    {user?.nickname ?? '관리자'}로 접속 중입니다. 권한이 없는 계정에서는 운영 메뉴가 숨겨집니다.
                                </div>
                            </div>
                        </div>
                        <div className="space-y-3">
                            <div
                                className={`grid gap-2.5 ${isAdmin ? 'grid-cols-2 sm:grid-cols-4 lg:grid-cols-2' : 'grid-cols-2 sm:grid-cols-3'}`}>
                                <MetricCard label="행사요청" value={String(summary.eventRequests)} dark/>
                                <MetricCard label="신고" value={String(summary.reports)} dark/>
                                <MetricCard label="채팅방" value={String(summary.rooms)} dark/>
                                {isAdmin && <MetricCard label="운영요청" value={String(summary.operationRequests)} dark/>}
                            </div>
                        </div>
                    </div>
                </section>
            )}

            <section
                className="rounded-[22px] border border-[var(--line)] bg-white p-3 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
                <div className="flex flex-wrap items-center gap-2">
                    {tabs.map((tab) => (
                        <button
                            key={tab.value}
                            type="button"
                            onClick={() => updateTab(tab.value)}
                            className={`rounded-full border px-2 py-1 text-[9px] font-semibold transition-colors md:px-2.5 md:py-1.5 md:text-[10px] ${
                                activeTab === tab.value
                                    ? 'border-[var(--accent)] bg-[var(--accent-soft)] text-[var(--accent)]'
                                    : 'border-[var(--line)] bg-white text-slate-600 hover:bg-slate-50'
                            }`}
                        >
                            <span>{tab.label}</span>
                            <span className="ml-1 text-[10px] font-medium opacity-70">{tab.description}</span>
                        </button>
                    ))}
                </div>
            </section>

            {activeTab === 'requests' && (
                <div className="space-y-6">
                    <div
                        className={`grid gap-6 ${canViewOperationRequests ? 'lg:grid-cols-[1.1fr_0.9fr]' : 'lg:grid-cols-1'}`}>
                        {canViewEventRequests && (
                            <section
                                className="space-y-4 rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
                                <SectionHeader
                                    title="행사요청"
                                    action={
                                        <div className="flex flex-wrap items-center gap-2">
                                            <button
                                                type="button"
                                                onClick={() => setGeneralCreateOpen((prev) => !prev)}
                                                className="rounded-full border border-[var(--line)] bg-white px-2 py-1 text-[9px] font-semibold text-slate-700 hover:bg-slate-50 md:px-2.5 md:py-1.5 md:text-[10px]"
                                            >
                                                {generalCreateOpen ? '일반 생성 닫기' : '일반 행사 생성'}
                                            </button>
                                            {REQUEST_STATUS_FILTERS.map((item) => (
                                                <Pill key={item} active={requestStatus === item}
                                                      onClick={() => setRequestStatus(item)}>
                                                    {item}
                                                </Pill>
                                            ))}
                                        </div>
                                    }
                                />

                                {managedEventCategories.length > 0 && (
                                    <div
                                        className="flex flex-wrap items-center gap-2 rounded-[18px] border border-dashed border-[var(--line)] bg-slate-50 px-4 py-3 text-xs text-slate-500">
                                        <span className="font-semibold text-slate-600">담당 카테고리</span>
                                        {managedEventCategories.map((item) => (
                                            <span key={item}
                                                  className="rounded-full bg-white px-2.5 py-1 font-semibold text-slate-600">
                      {item}
                    </span>
                                        ))}
                                    </div>
                                )}

                                {generalCreateOpen && (
                                    <EventCreatePanel
                                        title="일반 행사 생성"
                                        subtitle="관리자/매니저가 직접 전체 항목을 채워서 행사와 채팅방을 함께 만듭니다."
                                        draft={generalDraftWithCategory}
                                        setDraft={(patch) => setGeneralDraft((prev) => ({...prev, ...patch}))}
                                        categoryOptions={eventCategories as any[]}
                                        onSubmit={() => createEventMutation.mutate({draft: generalDraftWithCategory})}
                                        onClose={() => setGeneralCreateOpen(false)}
                                        loading={createEventMutation.isPending}
                                        onLookupPlace={() => {
                                            void lookupPlaceForDraft((patch) => setGeneralDraft((prev) => ({...prev, ...patch})))
                                        }}
                                        placeLookupLoading={addressLookupLoading}
                                        onAttachImage={(file) => {
                                            void attachImageForDraft(file, (patch) => setGeneralDraft((prev) => ({...prev, ...patch})))
                                        }}
                                    />
                                )}

                                <div className="space-y-3">
                                    {scopedEventRequests.map((request: any) => (
                                        <div key={request.id}
                                             className="rounded-[20px] border border-[var(--line)] bg-slate-50 p-4">
                                            <div
                                                className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                                                <div className="min-w-0 flex-1 space-y-2">
                                                    <div className="flex flex-wrap items-center gap-2">
                                                        <span
                                                            className="rounded-full bg-[var(--accent-soft)] px-2.5 py-1 text-[11px] font-semibold text-[var(--accent)]">{request.status}</span>
                                                        <span
                                                            className="text-xs text-slate-500">{request.category}</span>
                                                        {request.createdEventId && <span
                                                            className="rounded-full bg-emerald-100 px-2.5 py-1 text-[11px] font-semibold text-emerald-700">생성됨</span>}
                                                    </div>
                                                    <div
                                                        className="text-sm font-semibold text-slate-950">{request.eventName}</div>
                                                    <div
                                                        className="text-xs leading-5 text-slate-600">{request.description}</div>
                                                    {request.rejectReason && <div className="text-xs text-rose-600">반려
                                                        사유: {request.rejectReason}</div>}
                                                </div>
                                                <div className="flex shrink-0 flex-col gap-2 lg:w-[260px]">
                                                    {request.status === 'APPROVED' ? (
                                                        <>
                                                            <button
                                                                disabled
                                                                className="rounded-full border border-emerald-200 bg-emerald-50 px-4 py-2 text-sm font-semibold text-emerald-700 disabled:opacity-100"
                                                            >
                                                                승인 완료
                                                            </button>
                                                            {!request.createdEventId ? (
                                                                <button
                                                                    type="button"
                                                                    onClick={() => {
                                                                        setRequestDrafts((prev) =>
                                                                            prev[request.id]
                                                                                ? prev
                                                                                : {
                                                                                    ...prev,
                                                                                    [request.id]: buildRequestDraft(request, eventCategoryMap),
                                                                                },
                                                                        )
                                                                        setRequestPanels((prev) => ({
                                                                            ...prev,
                                                                            [request.id]: !prev[request.id]
                                                                        }))
                                                                    }}
                                                                    className="rounded-full bg-[var(--accent)] px-4 py-2 text-sm font-semibold text-white"
                                                                >
                                                                    행사 생성
                                                                </button>
                                                            ) : (
                                                                <Link
                                                                    to={`/events/${request.createdEventId}`}
                                                                    className="rounded-full border border-[var(--line)] bg-white px-4 py-2 text-center text-sm font-semibold text-slate-700"
                                                                >
                                                                    생성된 행사 보기
                                                                </Link>
                                                            )}
                                                        </>
                                                    ) : (
                                                        <>
                                                            <button
                                                                onClick={() => approveMutation.mutate(request.id)}
                                                                className="rounded-full bg-[var(--accent)] px-4 py-2 text-sm font-semibold text-white"
                                                            >
                                                                승인
                                                            </button>
                                                            <input
                                                                value={rejectReasons[request.id] ?? ''}
                                                                onChange={(e) => setRejectReasons((prev) => ({
                                                                    ...prev,
                                                                    [request.id]: e.target.value
                                                                }))}
                                                                placeholder="반려 사유"
                                                                className="rounded-full border border-[var(--line)] bg-white px-4 py-2 text-sm outline-none"
                                                            />
                                                            <button
                                                                onClick={() => {
                                                                    const reason = rejectReasons[request.id]?.trim()
                                                                    if (!reason) return
                                                                    rejectMutation.mutate({
                                                                        requestId: request.id,
                                                                        rejectReason: reason
                                                                    })
                                                                }}
                                                                className="rounded-full border border-[var(--line)] bg-white px-4 py-2 text-sm font-semibold text-slate-700"
                                                            >
                                                                반려
                                                            </button>
                                                        </>
                                                    )}
                                                </div>
                                            </div>

                                            {request.status === 'APPROVED' && requestPanels[request.id] && !request.createdEventId && (
                                                <EventCreatePanel
                                                    title="요청 정보로 행사 생성"
                                                    subtitle="요청 글의 내용을 자동으로 이어받아 필요한 항목만 채우면 됩니다."
                                                    draft={requestDrafts[request.id] ?? buildRequestDraft(request, eventCategoryMap)}
                                                    setDraft={(patch) => setRequestDrafts((prev) => mergeDraft(prev, request.id, patch))}
                                                    categoryOptions={eventCategories as any[]}
                                                    onSubmit={() =>
                                                        createEventMutation.mutate({
                                                            requestId: request.id,
                                                            draft: requestDrafts[request.id] ?? buildRequestDraft(request, eventCategoryMap),
                                                        })
                                                    }
                                                    onClose={() => setRequestPanels((prev) => ({
                                                        ...prev,
                                                        [request.id]: false
                                                    }))}
                                                    loading={createEventMutation.isPending}
                                                    onLookupPlace={() => {
                                                        void lookupPlaceForDraft((patch) => setRequestDrafts((prev) => mergeDraft(prev, request.id, patch)))
                                                    }}
                                                    placeLookupLoading={addressLookupLoading}
                                                    onAttachImage={(file) => {
                                                        void attachImageForDraft(file, (patch) => setRequestDrafts((prev) => mergeDraft(prev, request.id, patch)))
                                                    }}
                                                />
                                            )}
                                        </div>
                                    ))}
                                    {scopedEventRequests.length === 0 && <EmptyState text="처리할 행사 요청이 없습니다."/>}
                                </div>
                            </section>
                        )}

                        <section
                            className="space-y-4 rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
                            <SectionHeader
                                title="운영요청"
                                action={
                                    <div className="flex flex-wrap items-center gap-2">
                                        {OPERATION_STATUS_FILTERS.map((item) => (
                                            <Pill key={item} active={operationStatus === item}
                                                  onClick={() => setOperationStatus(item)}>
                                                {item}
                                            </Pill>
                                        ))}
                                    </div>
                                }
                            />
                            {canViewOperationRequests ? (
                                <div className="space-y-3">
                                    {scopedOperationRequests.map((request: OperationRequestItem) => {
                                        const form = operationForms[request.id] ?? {
                                            status: request.status,
                                            adminMemo: request.adminMemo ?? ''
                                        }
                                        return (
                                            <div key={request.id}
                                                 className="rounded-[20px] border border-[var(--line)] bg-slate-50 p-4">
                                                <div
                                                    className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                                                    <div className="min-w-0 flex-1 space-y-2">
                                                        <div className="flex flex-wrap items-center gap-2">
                                                            <span
                                                                className="rounded-full bg-[var(--accent-soft)] px-2.5 py-1 text-[11px] font-semibold text-[var(--accent)]">{request.status}</span>
                                                            <span className="text-xs text-slate-500">운영 요청</span>
                                                        </div>
                                                        <div
                                                            className="text-sm font-semibold text-slate-950">{request.title}</div>
                                                        <div
                                                            className="text-sm leading-6 text-slate-600">{request.content}</div>
                                                        <div className="text-xs text-slate-500">요청자
                                                            ID: {request.requesterId}</div>
                                                        {request.adminMemo && <div className="text-xs text-slate-500">관리
                                                            메모: {request.adminMemo}</div>}
                                                    </div>
                                                    <div className="flex shrink-0 flex-col gap-2 lg:w-[260px]">
                                                        <select
                                                            value={form.status}
                                                            onChange={(e) => setOperationForms((prev) => ({
                                                                ...prev,
                                                                [request.id]: {...form, status: e.target.value},
                                                            }))}
                                                            className="rounded-full border border-[var(--line)] bg-white px-4 py-2 text-sm outline-none"
                                                        >
                                                            {OPERATION_STATUS_FILTERS.filter((item) => item !== 'ALL').map((item) => (
                                                                <option key={item} value={item}>{item}</option>
                                                            ))}
                                                        </select>
                                                        <input
                                                            value={form.adminMemo}
                                                            onChange={(e) => setOperationForms((prev) => ({
                                                                ...prev,
                                                                [request.id]: {...form, adminMemo: e.target.value},
                                                            }))}
                                                            placeholder="관리 메모"
                                                            className="rounded-full border border-[var(--line)] bg-white px-4 py-2 text-sm outline-none"
                                                        />
                                                        <button
                                                            type="button"
                                                            onClick={() => operationRequestMutation.mutate({
                                                                requestId: request.id,
                                                                status: form.status,
                                                                adminMemo: form.adminMemo,
                                                            })}
                                                            className="rounded-full bg-[var(--accent)] px-4 py-2 text-sm font-semibold text-white"
                                                        >
                                                            상태 저장
                                                        </button>
                                                    </div>
                                                </div>
                                            </div>
                                        )
                                    })}
                                    {scopedOperationRequests.length === 0 && <EmptyState text="처리할 운영 요청이 없습니다."/>}
                                </div>
                            ) : (
                                <EmptyState text="운영 요청은 관리자만 볼 수 있어요."/>
                            )}
                        </section>
                    </div>

                    {canCreateCategories && (
                        <section ref={categoriesSectionRef} className="grid gap-6 lg:grid-cols-2">
                            <CategoryAdminPanel
                                title="행사 카테고리 생성"
                                subtitle="행사 요청 승인이나 일반 행사 생성에서 바로 고를 수 있는 카테고리를 관리합니다."
                                items={eventCategories as any[]}
                                draft={eventCategoryDraft}
                                setDraft={setEventCategoryDraft}
                                drafts={categoryDrafts}
                                setDrafts={(value) => setCategoryDrafts(value)}
                                canCreate={canCreateCategories}
                                onCreate={() => createEventCategoryMutation.mutate(eventCategoryDraft.trim())}
                                onUpdate={(categoryId, name) => updateEventCategoryMutation.mutate({categoryId, name})}
                                onDelete={(categoryId) => deleteEventCategoryMutation.mutate(categoryId)}
                                helperMessage={categoryErrors.event}
                                loading={createEventCategoryMutation.isPending || updateEventCategoryMutation.isPending || deleteEventCategoryMutation.isPending}
                            />
                            <CategoryAdminPanel
                                title="커뮤니티 카테고리 생성"
                                subtitle="후기, 꿀팁, 자유, 요청 같은 게시판 카테고리를 운영합니다."
                                items={communityCategories as any[]}
                                draft={communityCategoryDraft}
                                setDraft={setCommunityCategoryDraft}
                                drafts={categoryDrafts}
                                setDrafts={(value) => setCategoryDrafts(value)}
                                canCreate={canCreateCategories}
                                onCreate={() => createCommunityCategoryMutation.mutate(communityCategoryDraft.trim())}
                                onUpdate={(categoryId, name) => updateCommunityCategoryMutation.mutate({
                                    categoryId,
                                    name
                                })}
                                onDelete={(categoryId) => deleteCommunityCategoryMutation.mutate(categoryId)}
                                helperMessage={categoryErrors.community}
                                loading={createCommunityCategoryMutation.isPending || updateCommunityCategoryMutation.isPending || deleteCommunityCategoryMutation.isPending}
                            />
                        </section>
                    )}
                </div>
            )}

            {activeTab === 'reports' && (
                <section
                    className="space-y-4 rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
                    <SectionHeader
                        title="신고"
                        action={
                            <div className="flex flex-wrap gap-2">
                                {REPORT_STATUS_FILTERS.map((item) => (
                                    <Pill key={item} active={reportStatus === item}
                                          onClick={() => setReportStatus(item)}>
                                        {item}
                                    </Pill>
                                ))}
                            </div>
                        }
                    />

                    <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
                        {reports.map((report: any) => {
                            const form = reportForms[report.id] ?? {
                                status: report.status ?? 'RESOLVED',
                                operatorMemo: ''
                            }
                            const muted = report.status === 'RESOLVED' || report.status === 'REJECTED'
                            return (
                                <div key={report.id}
                                     className={`rounded-[20px] border border-[var(--line)] bg-slate-50 p-4 transition-opacity ${muted ? 'opacity-60' : ''}`}>
                                    <div className="flex flex-wrap items-center gap-2">
                                        <span
                                            className="rounded-full bg-rose-100 px-2.5 py-1 text-[11px] font-semibold text-rose-700">{report.status}</span>
                                        <span className="text-xs text-slate-500">{report.targetType}</span>
                                        <span className="text-xs text-slate-500">{report.category}</span>
                                    </div>
                                    <div
                                        className="mt-2 text-sm font-semibold text-slate-950">{report.description}</div>
                                    {report.targetContent && (
                                        <div
                                            className="mt-2 rounded-[16px] border border-[var(--line)] bg-white p-3 text-sm leading-6 text-slate-600">
                                            <div className="mb-1 text-[11px] font-semibold text-slate-500">원문 스냅샷</div>
                                            {report.targetContent}
                                        </div>
                                    )}
                                    <div className="mt-2 text-xs text-slate-500">대상 ID: {report.targetId}</div>
                                    <div className="mt-2 grid gap-2">
                                        <select
                                            value={form.status}
                                            onChange={(e) => setReportForms((prev) => ({
                                                ...prev,
                                                [report.id]: {...form, status: e.target.value},
                                            }))}
                                            className="rounded-full border border-[var(--line)] bg-white px-4 py-2 text-sm outline-none"
                                        >
                                            <option value="RESOLVED">RESOLVED</option>
                                            <option value="REJECTED">REJECTED</option>
                                        </select>
                                        <input
                                            value={form.operatorMemo}
                                            onChange={(e) => setReportForms((prev) => ({
                                                ...prev,
                                                [report.id]: {...form, operatorMemo: e.target.value},
                                            }))}
                                            placeholder="관리자 메모"
                                            className="rounded-full border border-[var(--line)] bg-white px-4 py-2 text-sm outline-none"
                                        />
                                        <button
                                            onClick={() => reportMutation.mutate({
                                                reportId: report.id,
                                                status: form.status,
                                                operatorMemo: form.operatorMemo.trim() || '처리 완료',
                                            })}
                                            className="rounded-full bg-[var(--accent)] px-4 py-2 text-sm font-semibold text-white"
                                        >
                                            처리 저장
                                        </button>
                                    </div>
                                    {report.operatorMemo &&
                                        <div className="mt-2 text-xs text-slate-500">기존 메모: {report.operatorMemo}</div>}
                                </div>
                            )
                        })}
                        {reports.length === 0 && <EmptyState text="신고 목록이 비어 있어요."/>}
                    </div>
                </section>
            )}

            {activeTab === 'chat' && (
                <div className="space-y-6">
                    <section className="grid gap-6 lg:grid-cols-[4fr_6fr]">
                        <div
                            className="space-y-4 rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
                            <SectionHeader
                                title="채팅방"
                                action={
                                    <div className="flex flex-wrap items-center gap-2">
                                        {CHAT_STATUS_FILTERS.map((item) => (
                                            <Pill key={item} active={chatStatus === item}
                                                  onClick={() => setChatStatus(item)}>
                                                {item}
                                            </Pill>
                                        ))}
                                    </div>
                                }
                            />
                            <div className="space-y-3">
                                {scopedChatRooms.map((room: any) => (
                                    <div key={room.chatRoomId}
                                         className="rounded-[20px] border border-[var(--line)] bg-slate-50 p-4">
                                        <div
                                            className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                                            <div className="min-w-0 flex-1">
                                                <div className="flex flex-wrap items-center gap-2">
                                                    <span
                                                        className="rounded-full bg-[var(--accent-soft)] px-2.5 py-1 text-[11px] font-semibold text-[var(--accent)]">{room.category}</span>
                                                    <span
                                                        className="rounded-full bg-slate-100 px-2.5 py-1 text-[11px] font-semibold text-slate-600">{room.status}</span>
                                                </div>
                                                <div
                                                    className="mt-2 text-sm font-semibold text-slate-950">{room.eventName}</div>
                                                <div className="mt-2 grid gap-1 text-xs text-slate-500">
                                                    <div>예정
                                                        오픈: {room.scheduledOpenAt ? formatDateTime(room.scheduledOpenAt) : '정보 없음'}</div>
                                                    <div>예정
                                                        종료: {room.scheduledCloseAt ? formatDateTime(room.scheduledCloseAt) : '정보 없음'}</div>
                                                    <div>현재 인원: {room.currentViewerCount ?? 0}명</div>
                                                </div>
                                            </div>
                                            <div className="flex shrink-0 gap-2">
                                                <button
                                                    onClick={() => chatRoomMutation.mutate({
                                                        chatRoomId: room.chatRoomId,
                                                        action: 'FORCE_OPEN'
                                                    })}
                                                    className="rounded-full bg-[var(--accent)] px-4 py-2 text-sm font-semibold text-white"
                                                >
                                                    강제 열기
                                                </button>
                                                <button
                                                    onClick={() => chatRoomMutation.mutate({
                                                        chatRoomId: room.chatRoomId,
                                                        action: 'FORCE_CLOSE'
                                                    })}
                                                    className="rounded-full border border-[var(--line)] bg-white px-4 py-2 text-sm font-semibold text-slate-700"
                                                >
                                                    강제 닫기
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                                {scopedChatRooms.length === 0 && <EmptyState text="조회할 채팅방이 없습니다."/>}
                            </div>
                        </div>

                        <section
                            className="space-y-3 rounded-[24px] border border-[var(--line)] bg-white p-4 shadow-[0_12px_30px_rgba(15,23,42,0.04)] md:p-5">
                            <SectionHeader
                                title="메시지 모니터링"
                                action={
                                    <div className="flex flex-wrap items-center gap-2">
                                        <button
                                            type="button"
                                            onClick={() => {
                                                void refetchAdminMessages()
                                            }}
                                            className="rounded-full border border-[var(--line)] bg-white px-2 py-1 text-[9px] font-semibold text-slate-700 hover:bg-slate-50 md:px-2.5 md:py-1.5 md:text-[10px]"
                                        >
                                            새로고침
                                        </button>
                                        <Pill
                                            active={messageStatus === 'ALL'}
                                            onClick={() => {
                                                setMessageStatus('ALL')
                                                setMessagePage(0)
                                            }}
                                        >
                                            ALL
                                        </Pill>
                                        <Pill
                                            active={messageStatus === 'ACTIVE'}
                                            onClick={() => {
                                                setMessageStatus('ACTIVE')
                                                setMessagePage(0)
                                            }}
                                        >
                                            ACTIVE
                                        </Pill>
                                        <Pill
                                            active={messageStatus === 'BLINDED'}
                                            onClick={() => {
                                                setMessageStatus('BLINDED')
                                                setMessagePage(0)
                                            }}
                                        >
                                            BLINDED
                                        </Pill>
                                    </div>
                                }
                            />
                            <div className="space-y-2">
                                {adminMessages.map((message: AdminMessageItem) => (
                                    <div key={message.messageId}
                                         className="rounded-[16px] border border-[var(--line)] bg-slate-50 px-3 py-3">
                                        <div className="flex items-start justify-between gap-3">
                                            <div className="min-w-0 flex-1">
                                                <div
                                                    className="flex flex-wrap items-center gap-1.5 text-[10px] font-semibold">
                                                    <span
                                                        className="rounded-full bg-[var(--accent-soft)] px-2 py-0.5 text-[10px] text-[var(--accent)]">{message.status}</span>
                                                    <span
                                                        className="rounded-full bg-slate-100 px-2 py-0.5 text-[10px] text-slate-600">{message.messageType}</span>
                                                    <span
                                                        className="rounded-full bg-slate-100 px-2 py-0.5 text-[10px] text-slate-600">{message.writerNickname || '알 수 없음'}</span>
                                                </div>
                                                <div
                                                    className="mt-2 text-sm leading-6 text-slate-800">{message.content}</div>
                                                <div className="mt-2 text-[11px] text-slate-500">
                                                    채팅방 ID: {message.chatRoomId} · {formatDateTime(message.createdAt)}
                                                </div>
                                            </div>
                                            <div className="flex shrink-0 flex-col gap-2">
                                                <button
                                                    type="button"
                                                    onClick={() => messageMutation.mutate({
                                                        messageId: message.messageId,
                                                        status: 'ACTIVE'
                                                    })}
                                                    className="rounded-full border border-[var(--line)] bg-white px-2.5 py-1.5 text-[11px] font-semibold text-slate-700"
                                                >
                                                    활성화
                                                </button>
                                                <button
                                                    type="button"
                                                    onClick={() => messageMutation.mutate({
                                                        messageId: message.messageId,
                                                        status: 'BLINDED'
                                                    })}
                                                    className="rounded-full border border-[var(--line)] bg-white px-2.5 py-1.5 text-[11px] font-semibold text-slate-700"
                                                >
                                                    블라인드
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                                {adminMessages.length === 0 && <EmptyState text="관리할 메시지가 없습니다."/>}
                            </div>

                            <div className="flex items-center justify-center gap-2 pt-2">
                                <button
                                    type="button"
                                    disabled={messagePage === 0}
                                    onClick={() => setMessagePage((prev) => Math.max(prev - 1, 0))}
                                    className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:opacity-40"
                                >
                                    이전
                                </button>
                                <div className="flex items-center gap-1">
                                    {Array.from({length: messageTotalPages}, (_, index) => index).map((pageIndex) => (
                                        <button
                                            key={pageIndex}
                                            type="button"
                                            onClick={() => setMessagePage(pageIndex)}
                                            className={`h-8 min-w-8 rounded-full px-2 text-xs font-semibold transition-colors ${
                                                messagePage === pageIndex ? 'bg-[var(--accent-soft)] text-[var(--accent)]' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                                            }`}
                                        >
                                            {pageIndex + 1}
                                        </button>
                                    ))}
                                </div>
                                <button
                                    type="button"
                                    disabled={messagePage + 1 >= messageTotalPages}
                                    onClick={() => setMessagePage((prev) => prev + 1)}
                                    className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:opacity-40"
                                >
                                    다음
                                </button>
                            </div>
                        </section>
                    </section>
                </div>
            )}
        </div>
    )
}

function MetricCard({label, value, dark = false}: { label: string; value: string; dark?: boolean }) {
    return (
        <div
            className={dark ? 'rounded-[18px] border border-white/10 bg-white/5 px-4 py-3' : 'rounded-[18px] border border-[var(--line)] bg-slate-50 px-4 py-3'}>
            <div
                className={dark ? 'text-xs font-medium text-slate-400' : 'text-xs font-medium text-slate-500'}>{label}</div>
            <div
                className={dark ? 'mt-1 text-[24px] font-black tracking-tight text-white' : 'mt-1 text-[24px] font-black tracking-tight text-slate-950'}>{value}</div>
        </div>
    )
}

function SectionHeader({title, action}: { title: string; action?: ReactNode }) {
    return (
        <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between sm:gap-3">
            <h2 className="whitespace-nowrap text-[17px] font-black tracking-tight text-slate-950 sm:text-[18px]">{title}</h2>
            {action}
        </div>
    )
}

function Pill({active, onClick, children}: { active?: boolean; onClick: () => void; children: ReactNode }) {
    return (
        <button
            onClick={onClick}
            className={`rounded-full px-2 py-1 text-[9px] font-semibold transition-colors md:px-2.5 md:py-1.5 md:text-[10px] ${
                active ? 'bg-[var(--accent-soft)] text-[var(--accent)]' : 'bg-slate-50 text-slate-600 hover:bg-slate-100'
            }`}
        >
            {children}
        </button>
    )
}

function EmptyState({text}: { text: string }) {
    return <div
        className="rounded-[18px] border border-dashed border-[var(--line)] bg-white px-4 py-5 text-sm text-slate-500">{text}</div>
}

const EVENT_CATEGORY_TO_CHAT: Record<string, string> = {
    concert: 'CONCERT',
    festival: 'FESTIVAL',
    fanmeeting: 'FANMEETING',
    popup: 'POPUP',
}

interface EventDraft {
    eventName: string
    categoryId: string
    startAt: string
    endAt: string
    place: string
    region: string
    latitude: string
    longitude: string
    radius: string
    minFee: string
    maxFee: string
    hasTicketing: boolean
    ticketingOpenAt: string
    ticketingCloseAt: string
    ticketingLink: string
    officialLink: string
    performer: string
    description: string
    img: string
    imgFileName: string
    schedules: Array<{
        name: string
        startAt: string
        endAt: string
    }>
}

function createBlankEventDraft(): EventDraft {
    return {
        eventName: '',
        categoryId: '',
        startAt: '',
        endAt: '',
        place: '',
        region: '',
        latitude: '',
        longitude: '',
        radius: '',
        minFee: '',
        maxFee: '',
        hasTicketing: true,
        ticketingOpenAt: '',
        ticketingCloseAt: '',
        ticketingLink: '',
        officialLink: '',
        performer: '',
        description: '',
        img: '',
        imgFileName: '',
        schedules: [],
    }
}

function buildRequestDraft(request: any, categoryMap: Map<string, string>): EventDraft {
    return {
        ...createBlankEventDraft(),
        eventName: request.eventName ?? '',
        categoryId: categoryMap.get(request.category) ?? categoryMap.values().next().value ?? '',
        ticketingLink: request.link ?? '',
        description: request.description ?? '',
        schedules: [],
    }
}

function mergeDraft(drafts: Record<string, EventDraft>, requestId: string, patch: Partial<EventDraft>) {
    const current = drafts[requestId] ?? createBlankEventDraft()
    return {
        ...drafts,
        [requestId]: {...current, ...patch},
    }
}

function toRequiredNumber(value: string) {
    const trimmed = value.trim()
    if (!trimmed) return null
    const parsed = Number(trimmed)
    return Number.isFinite(parsed) ? parsed : null
}

function toOptionalNumber(value: string) {
    const trimmed = value.trim()
    if (!trimmed) return null
    const parsed = Number(trimmed)
    return Number.isFinite(parsed) ? parsed : null
}

function normalizeDateTimeInput(value: string) {
    const trimmed = value.trim()
    if (!trimmed) return null
    if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(trimmed)) return `${trimmed}:00`
    return trimmed
}

function readFileAsDataUrl(file: File) {
    return new Promise<string>((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = () => resolve(String(reader.result ?? ''))
        reader.onerror = () => reject(reader.error ?? new Error('파일을 읽지 못했습니다.'))
        reader.readAsDataURL(file)
    })
}

function AdminInput({
                        label,
                        value,
                        onChange,
                        placeholder,
                        type = 'text',
                    }: {
    label: string
    value: string
    onChange: (value: string) => void
    placeholder?: string
    type?: string
}) {
    const inputClassName =
        type === 'datetime-local'
            ? 'w-full rounded-full border border-[var(--line)] bg-slate-50 px-3 py-2 text-sm text-slate-500 outline-none [color-scheme:light]'
            : 'w-full rounded-full border border-[var(--line)] bg-slate-50 px-3 py-2 text-sm outline-none'

    return (
        <label className="block">
            <div className="mb-1 text-[11px] font-semibold text-slate-500">{label}</div>
            <input
                value={value}
                type={type}
                onChange={(e) => onChange(e.target.value)}
                placeholder={placeholder}
                className={inputClassName}
            />
        </label>
    )
}

function EventCreatePanel({
                              title,
                              subtitle,
                              draft,
                              setDraft,
                              categoryOptions,
                              onSubmit,
                              onClose,
                              loading,
                              onLookupPlace,
                              placeLookupLoading,
                              onAttachImage,
                          }: {
    title: string
    subtitle: string
    draft: EventDraft
    setDraft: (patch: Partial<EventDraft>) => void
    categoryOptions: any[]
    onSubmit: () => void
    onClose: () => void
    loading?: boolean
    onLookupPlace: () => void
    placeLookupLoading?: boolean
    onAttachImage: (file: File) => void
}) {
    const categoryValue = draft.categoryId || categoryOptions[0]?.id || ''
    const imageInputRef = useRef<HTMLInputElement | null>(null)
    const hasCoordinates = Boolean(draft.latitude.trim()) && Boolean(draft.longitude.trim())
    const hasPlace = Boolean(draft.place.trim())
    const hasLocationAttempt = Boolean(draft.place.trim() || draft.latitude.trim() || draft.longitude.trim())
    const showLocationWarning = hasLocationAttempt && (!hasPlace || !hasCoordinates)
    const canSubmit = hasPlace && hasCoordinates

    return (
        <div className="rounded-[22px] border border-[var(--line)] bg-white p-4">
            <div className="flex items-start justify-between gap-3">
                <div>
                    <div className="text-sm font-semibold text-slate-950">{title}</div>
                    <div className="mt-1 text-xs leading-5 text-slate-500">{subtitle}</div>
                </div>
                <button type="button" onClick={onClose}
                        className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-600">
                    닫기
                </button>
            </div>

            <div className="mt-4 grid gap-3 md:grid-cols-2">
                <AdminInput label="행사명" value={draft.eventName} onChange={(value) => setDraft({eventName: value})}/>
                <label className="block">
                    <div className="mb-1 text-[11px] font-semibold text-slate-500">카테고리</div>
                    <select
                        value={categoryValue}
                        onChange={(e) => setDraft({categoryId: e.target.value})}
                        className="w-full rounded-full border border-[var(--line)] bg-slate-50 px-3 py-2.5 text-sm outline-none"
                    >
                        <option value="">선택하세요</option>
                        {categoryOptions.map((category) => (
                            <option key={category.id} value={category.id}>
                                {category.name}
                            </option>
                        ))}
                    </select>
                </label>
                <AdminInput label="시작일" value={draft.startAt} onChange={(value) => setDraft({startAt: value})}
                            type="datetime-local"/>
                <AdminInput label="종료일" value={draft.endAt} onChange={(value) => setDraft({endAt: value})}
                            type="datetime-local"/>
                <label className="md:col-span-2 block">
                    <div
                        className="mb-1 flex items-center justify-between gap-2 text-[11px] font-semibold text-slate-500">
                        <span>장소</span>
                        <button
                            type="button"
                            onClick={() => {
                                if (placeLookupLoading) return
                                onLookupPlace()
                            }}
                            aria-busy={placeLookupLoading}
                            className="rounded-full border border-[var(--line)] bg-white px-3 py-1 text-[10px] font-semibold text-[var(--accent)]"
                        >
                            {placeLookupLoading ? '검색 중...' : '카카오 주소검색'}
                        </button>
                    </div>
                    <input
                        value={draft.place}
                        onChange={(e) =>
                            setDraft({
                                place: e.target.value,
                                region: '',
                                latitude: '',
                                longitude: '',
                            })
                        }
                        placeholder="주소를 검색하거나 직접 입력하세요"
                        className="w-full rounded-full border border-[var(--line)] bg-slate-50 px-3 py-2 text-sm outline-none"
                    />
                    <div className="mt-1 text-[11px] leading-5 text-slate-400">
                        주소를 다시 바꾸면 지역, 위도, 경도는 비워져요. 다시 검색해서 자동 입력하면 돼요.
                    </div>
                    {showLocationWarning && (
                        <div className="mt-2 rounded-[14px] border border-amber-200 bg-amber-50 px-3 py-2 text-[11px] leading-5 text-amber-700">
                            {!hasPlace
                                ? '장소를 입력하거나 카카오 주소검색을 먼저 눌러주세요.'
                                : '주소는 입력됐지만 위도/경도가 비어 있어요. 카카오 주소검색을 다시 눌러 자동 입력하거나, 위도/경도를 직접 채워주세요.'}
                        </div>
                    )}
                </label>
                <AdminInput label="지역" value={draft.region} onChange={(value) => setDraft({region: value})}
                            placeholder="자동 입력"/>
                <AdminInput label="위도" value={draft.latitude} onChange={(value) => setDraft({latitude: value})}
                            placeholder="37.5665" type="number"/>
                <AdminInput label="경도" value={draft.longitude} onChange={(value) => setDraft({longitude: value})}
                            placeholder="126.9780" type="number"/>
                <AdminInput label="반경" value={draft.radius} onChange={(value) => setDraft({radius: value})}
                            placeholder="500" type="number"/>
                <AdminInput label="최소 금액" value={draft.minFee} onChange={(value) => setDraft({minFee: value})}
                            placeholder="0" type="number"/>
                <AdminInput label="최대 금액" value={draft.maxFee} onChange={(value) => setDraft({maxFee: value})}
                            placeholder="150000" type="number"/>
                <AdminInput label="공식 링크" value={draft.officialLink}
                            onChange={(value) => setDraft({officialLink: value})}/>
                <AdminInput label="출연" value={draft.performer} onChange={(value) => setDraft({performer: value})}/>
                <div className="md:col-span-2">
                    <div className="mb-1 text-[11px] font-semibold text-slate-500">티켓팅 여부</div>
                    <div className="flex flex-wrap gap-2">
                        <TogglePill active={draft.hasTicketing}
                                    onClick={() => setDraft({hasTicketing: true})}>있음</TogglePill>
                        <TogglePill active={!draft.hasTicketing}
                                    onClick={() => setDraft({hasTicketing: false})}>없음</TogglePill>
                    </div>
                </div>
                {draft.hasTicketing && (
                    <div className="md:col-span-2 space-y-3 rounded-[20px] border border-[var(--line)] bg-slate-50 p-4">
                        <div className="text-[11px] font-semibold text-slate-500">티켓팅 정보</div>
                        <div className="grid gap-3 md:grid-cols-2">
                            <AdminInput label="티켓팅 오픈" value={draft.ticketingOpenAt}
                                        onChange={(value) => setDraft({ticketingOpenAt: value})} type="datetime-local"/>
                            <AdminInput label="티켓팅 종료" value={draft.ticketingCloseAt}
                                        onChange={(value) => setDraft({ticketingCloseAt: value})}
                                        type="datetime-local"/>
                            <div className="md:col-span-2">
                                <AdminInput label="티켓팅 링크" value={draft.ticketingLink}
                                            onChange={(value) => setDraft({ticketingLink: value})}/>
                            </div>
                        </div>
                    </div>
                )}
                <label className="md:col-span-2 block">
                    <div
                        className="mb-1 flex items-center justify-between gap-2 text-[11px] font-semibold text-slate-500">
                        <span>대표 이미지</span>
                        <button
                            type="button"
                            onClick={() => imageInputRef.current?.click()}
                            className="rounded-full border border-[var(--line)] bg-white px-3 py-1 text-[10px] font-semibold text-[var(--accent)]"
                        >
                            파일 첨부
                        </button>
                    </div>
                    <input
                        ref={imageInputRef}
                        type="file"
                        accept="image/*"
                        className="hidden"
                        onChange={(e) => {
                            const file = e.target.files?.[0]
                            if (file) onAttachImage(file)
                        }}
                    />
                    <div className="flex items-center gap-2">
                        <input
                            value={draft.img}
                            onChange={(e) => setDraft({img: e.target.value, imgFileName: ''})}
                            placeholder="https://... 또는 파일 첨부"
                            className="min-w-0 flex-1 rounded-full border border-[var(--line)] bg-slate-50 px-3 py-2 text-sm outline-none"
                        />
                    </div>
                    <div className="mt-1 text-[11px] leading-5 text-slate-400">
                        {draft.imgFileName ? `첨부 파일: ${draft.imgFileName}` : 'URL 또는 파일 첨부 둘 다 사용할 수 있어요.'}
                    </div>
                </label>

                <div className="md:col-span-2 flex items-center justify-between gap-3">
                    <div className="text-[11px] font-semibold text-slate-500">일정</div>
                    <button
                        type="button"
                        onClick={() =>
                            setDraft({
                                schedules: [...(draft.schedules ?? []), {
                                    name: `Day ${(draft.schedules?.length ?? 0) + 1}`,
                                    startAt: '',
                                    endAt: ''
                                }],
                            })
                        }
                        className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-[10px] font-semibold text-[var(--accent)]"
                    >
                        + 일정
                    </button>
                </div>

                <div className="md:col-span-2 space-y-2">
                    {(draft.schedules ?? []).map((schedule, index) => (
                        <div key={index}
                             className="grid gap-2 rounded-[18px] border border-[var(--line)] bg-slate-50 p-3 sm:grid-cols-2 xl:grid-cols-[1.2fr_1fr_1fr_auto] xl:items-end">
                            <div className="sm:col-span-2 xl:col-span-1">
                                <AdminInput
                                    label={`일정 제목 ${index + 1}`}
                                    value={schedule.name}
                                    onChange={(value) =>
                                        setDraft({
                                            schedules: (draft.schedules ?? []).map((item, itemIndex) => (itemIndex === index ? {
                                                ...item,
                                                name: value
                                            } : item)),
                                        })
                                    }
                                />
                            </div>
                            <AdminInput
                                label="일정 시작"
                                value={schedule.startAt}
                                onChange={(value) =>
                                    setDraft({
                                        schedules: (draft.schedules ?? []).map((item, itemIndex) => (itemIndex === index ? {
                                            ...item,
                                            startAt: value
                                        } : item)),
                                    })
                                }
                                type="datetime-local"
                            />
                            <AdminInput
                                label="일정 종료"
                                value={schedule.endAt}
                                onChange={(value) =>
                                    setDraft({
                                        schedules: (draft.schedules ?? []).map((item, itemIndex) => (itemIndex === index ? {
                                            ...item,
                                            endAt: value
                                        } : item)),
                                    })
                                }
                                type="datetime-local"
                            />
                            <button
                                type="button"
                                onClick={() =>
                                    setDraft({
                                        schedules: (draft.schedules ?? []).filter((_, itemIndex) => itemIndex !== index),
                                    })
                                }
                                className="w-full rounded-full border border-rose-200 bg-rose-50 px-3 py-2 text-xs font-semibold text-rose-700 sm:col-span-2 xl:col-span-1"
                            >
                                삭제
                            </button>
                        </div>
                    ))}
                    {(!draft.schedules || draft.schedules.length === 0) && (
                        <div
                            className="rounded-[18px] border border-dashed border-[var(--line)] bg-white px-4 py-5 text-sm text-slate-500">
                            세부 일정이 없으면 비워도 돼요. 상단 시작일/종료일이 행사 기본 일정이 됩니다.
                        </div>
                    )}
                </div>
                <label className="md:col-span-2 block">
                    <div className="mb-1 text-[11px] font-semibold text-slate-500">설명</div>
                    <textarea
                        value={draft.description}
                        onChange={(e) => setDraft({description: e.target.value})}
                        className="min-h-24 w-full rounded-[18px] border border-[var(--line)] bg-slate-50 p-3 text-sm outline-none"
                    />
                </label>
            </div>

            <div className="mt-4 flex items-center justify-end gap-2">
                {!canSubmit && (
                    <div className="mr-auto rounded-full border border-amber-200 bg-amber-50 px-3 py-2 text-xs font-semibold text-amber-700">
                        장소와 위도/경도를 모두 채워야 행사 등록이 가능해요.
                    </div>
                )}
                <button
                    type="button"
                    onClick={onClose}
                    className="rounded-full border border-[var(--line)] bg-white px-4 py-2 text-sm font-semibold text-slate-700"
                >
                    닫기
                </button>
                <button
                    type="button"
                    onClick={onSubmit}
                    disabled={loading || !canSubmit}
                    className="rounded-full bg-[var(--accent)] px-4 py-2 text-sm font-semibold text-white disabled:opacity-70"
                >
                    {loading ? '생성 중...' : '행사 등록'}
                </button>
            </div>
        </div>
    )
}

function CategoryAdminPanel({
                                title,
                                subtitle,
                                items,
                                draft,
                                setDraft,
                                drafts,
                                setDrafts,
                                canCreate,
                                onCreate,
                                onUpdate,
                                onDelete,
                                helperMessage,
                                loading,
                            }: {
    title: string
    subtitle: string
    items: any[]
    draft: string
    setDraft: (value: string) => void
    drafts: Record<string, string>
    setDrafts: (value: Record<string, string>) => void
    canCreate: boolean
    onCreate: () => void
    onUpdate: (categoryId: string, name: string) => void
    onDelete: (categoryId: string) => void
    helperMessage?: string
    loading?: boolean
}) {
    return (
        <section
            className="space-y-4 rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
            <SectionHeader title={title} action={<span className="text-xs text-slate-500">{items.length}개</span>}/>
            <p className="text-sm leading-6 text-slate-600">{subtitle}</p>
            {canCreate ? (
                <div className="flex gap-2">
                    <input
                        value={draft}
                        onChange={(e) => setDraft(e.target.value)}
                        placeholder="카테고리 이름"
                        className="min-w-0 flex-1 rounded-full border border-[var(--line)] bg-slate-50 px-4 py-2.5 text-sm outline-none"
                    />
                    <button
                        type="button"
                        disabled={loading || !draft.trim()}
                        onClick={onCreate}
                        className="rounded-full bg-[var(--accent-soft)] px-4 py-2.5 text-sm font-semibold text-[var(--accent)] disabled:opacity-60"
                    >
                        생성
                    </button>
                </div>
            ) : (
                <div
                    className="rounded-[18px] border border-dashed border-[var(--line)] bg-slate-50 px-4 py-3 text-xs font-medium text-slate-500">
                    카테고리 생성은 ADMIN만 가능합니다. 목록 수정/삭제는 현재 권한 범위에 따라 가능해요.
                </div>
            )}
            {helperMessage ? <div className="text-xs font-medium text-rose-600">{helperMessage}</div> : null}

            <div className="space-y-2">
                {items.map((item) => (
                    <div key={item.id}
                         className="flex flex-col gap-2 rounded-[18px] border border-[var(--line)] bg-slate-50 p-3 md:flex-row md:items-center">
                        <input
                            value={drafts[item.id] ?? item.name}
                            onChange={(e) => setDrafts({...drafts, [item.id]: e.target.value})}
                            className="min-w-0 flex-1 rounded-full border border-[var(--line)] bg-white px-3 py-2 text-sm outline-none"
                        />
                        <div className="flex gap-2">
                            <button
                                type="button"
                                onClick={() => onUpdate(item.id, (drafts[item.id] ?? item.name).trim() || item.name)}
                                className="rounded-full border border-[var(--line)] bg-white px-3 py-2 text-xs font-semibold text-slate-700"
                            >
                                저장
                            </button>
                            <button
                                type="button"
                                onClick={() => onDelete(item.id)}
                                className="rounded-full border border-rose-200 bg-rose-50 px-3 py-2 text-xs font-semibold text-rose-700"
                            >
                                삭제
                            </button>
                        </div>
                    </div>
                ))}
                {items.length === 0 && <EmptyState text="등록된 카테고리가 없습니다."/>}
            </div>
        </section>
    )
}

function TogglePill({active, onClick, children}: { active?: boolean; onClick: () => void; children: ReactNode }) {
    return (
        <button
            type="button"
            onClick={onClick}
            className={`rounded-full px-3 py-2 text-xs font-semibold transition-colors ${
                active ? 'bg-[var(--accent-soft)] text-[var(--accent)]' : 'bg-slate-100 text-slate-500 hover:bg-slate-200'
            }`}
        >
            {children}
        </button>
    )
}

function normalizeRole(role?: string | null) {
    if (!role) return ''
    return role.replace(/^ROLE_/, '')
}

function normalizeAdminTab(value: string | null): AdminTab {
    if (value === 'reports' || value === 'chat' || value === 'requests') return value
    return 'requests'
}

function normalizeEventCategoryKey(name: string) {
    if (name === '\uCF58\uC11C\uD2B8') return 'concert'
    if (name === '\uCD95\uC81C') return 'festival'
    if (name === '\uD32C\uBBF8\uD305') return 'fanmeeting'
    if (name === '\uD31D\uC5C5\uC2A4\uD1A0\uC5B4') return 'popup'
    return name.toLowerCase()
}

function extractErrorMessage(error: any, fallback: string) {
    return error?.response?.data?.message
        ?? error?.response?.data?.error
        ?? error?.message
        ?? fallback
}
