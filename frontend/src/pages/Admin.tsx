import {type ReactNode, useEffect, useMemo, useRef, useState} from 'react'
import {Link, useNavigate, useSearchParams} from 'react-router-dom'
import {useMutation, useQueries, useQuery, useQueryClient} from '@tanstack/react-query'
import {
    approveEventRequest,
    getAdminChatRooms,
    getAdminUsers,
    getAdminUserDetail,
    createBlacklist,
    getBlacklists,
    forceChatRoomStatus,
    getEventRequests,
    getOperationRequests,
    releaseBlacklist,
  getReports,
  rejectEventRequest,
    changeAdminUserRole,
    updateOperationRequestStatus,
    updateReportStatus,
} from '../api/admin'
import { deleteNotification, getNotifications, markAllNotificationsAsRead } from '../api/notifications'
import {createEvent, getEvent, updateEvent} from '../api/events'
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
import {getErrorMessage} from '../lib/error'
import {searchAddressWithKakao} from '../lib/kakao'
import type {Event} from '../types'
import type {AdminMessageItem, AdminUserDetailItem, AdminUserItem, BlacklistPage, OperationRequestItem, AdminUserRole, AdminUserStatus, BlacklistStatus, ReportPage, ReportItem} from '../types/admin'

const REPORT_STATUS_FILTERS = ['ALL', 'AUTO_BLINDED', 'RESOLVED', 'REJECTED'] as const
const REQUEST_STATUS_FILTERS = ['ALL', 'PENDING', 'APPROVED', 'REJECTED', 'CANCELED'] as const
const OPERATION_STATUS_FILTERS = ['ALL', 'PENDING', 'IN_PROGRESS', 'RESOLVED', 'REJECTED'] as const
const CHAT_STATUS_FILTERS = ['ALL', 'SCHEDULED', 'OPEN', 'CLOSED'] as const
const USER_ROLE_FILTERS = ['ALL', 'USER', 'ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER', 'COMMUNITY_MANAGER'] as const
const ADMIN_TABS = ['manage', 'reports', 'chat', 'users', 'blacklist', 'notifications'] as const
const EVENT_CATEGORY_SCOPE: Record<string, string[]> = {
    ADMIN: [],
    CONCERT_MANAGER: ['\uCF58\uC11C\uD2B8'],
    FESTIVAL_MANAGER: ['\uCD95\uC81C'],
    FANMEETING_MANAGER: ['\uD32C\uBBF8\uD305'],
    POPUP_MANAGER: ['\uD31D\uC5C5\uC2A4\uD1A0\uC5B4'],
}

type AdminTab = (typeof ADMIN_TABS)[number]
type EventFormFieldErrorKey =
    | 'eventName'
    | 'categoryId'
    | 'startAt'
    | 'endAt'
    | 'place'
    | 'region'
    | 'latitude'
    | 'longitude'
    | 'radius'
    | 'minFee'
    | 'maxFee'
    | 'ticketingOpenAt'
    | 'ticketingCloseAt'
    | 'ticketingLink'
    | 'officialLink'
    | 'performer'
    | 'description'
    | 'img'

type EventFormErrors = Partial<Record<EventFormFieldErrorKey, string>>

const USER_ROLE_OPTIONS: { value: AdminUserRole | 'ALL'; label: string }[] = [
    {value: 'ALL', label: '전체'},
    {value: 'USER', label: '일반 사용자'},
    {value: 'ADMIN', label: '관리자'},
    {value: 'CONCERT_MANAGER', label: '콘서트 매니저'},
    {value: 'FESTIVAL_MANAGER', label: '축제 매니저'},
    {value: 'FANMEETING_MANAGER', label: '팬미팅 매니저'},
    {value: 'POPUP_MANAGER', label: '팝업 매니저'},
    {value: 'COMMUNITY_MANAGER', label: '커뮤니티 매니저'},
]

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
    const showLegacyManageSection = searchParams.get('legacyManage') === '1'
    const [reportStatus, setReportStatus] = useState<(typeof REPORT_STATUS_FILTERS)[number]>('ALL')
    const [requestStatus, setRequestStatus] = useState<(typeof REQUEST_STATUS_FILTERS)[number]>('ALL')
    const [operationStatus, setOperationStatus] = useState<(typeof OPERATION_STATUS_FILTERS)[number]>('ALL')
    const [chatStatus, setChatStatus] = useState<(typeof CHAT_STATUS_FILTERS)[number]>('ALL')
    const [messageStatus, setMessageStatus] = useState<'ALL' | 'ACTIVE' | 'BLINDED'>('ALL')
    const [messagePage, setMessagePage] = useState(0)
    const [reportPage, setReportPage] = useState(0)
    const [userSearchEmail, setUserSearchEmail] = useState('')
    const [userSearchName, setUserSearchName] = useState('')
    const [userSearchRole, setUserSearchRole] = useState<(typeof USER_ROLE_FILTERS)[number]>('ALL')
    const [submittedUserSearchEmail, setSubmittedUserSearchEmail] = useState('')
    const [submittedUserSearchName, setSubmittedUserSearchName] = useState('')
    const [submittedUserSearchRole, setSubmittedUserSearchRole] = useState<(typeof USER_ROLE_FILTERS)[number]>('ALL')
    const [userPage, setUserPage] = useState(0)
    const [selectedUserId, setSelectedUserId] = useState<string | null>(null)
    const [blacklistStatus, setBlacklistStatus] = useState<'ALL' | BlacklistStatus>('ALL')
    const [blacklistSearch, setBlacklistSearch] = useState('')
    const [blacklistSubmittedSearch, setBlacklistSubmittedSearch] = useState('')
    const [blacklistPage, setBlacklistPage] = useState(0)
    const [selectedBlacklistId, setSelectedBlacklistId] = useState<string | null>(null)
    const [adminNotificationPage, setAdminNotificationPage] = useState(0)
    const [rejectReasons, setRejectReasons] = useState<Record<string, string>>({})
    const [reportReviewForms, setReportReviewForms] = useState<Record<string, { status: string; operatorMemo: string }>>({})
    const [operationForms, setOperationForms] = useState<Record<string, { status: string; adminMemo: string }>>({})
    const [requestPanels, setRequestPanels] = useState<Record<string, boolean>>({})
    const [requestDrafts, setRequestDrafts] = useState<Record<string, EventDraft>>({})
    const [generalCreateOpen, setGeneralCreateOpen] = useState(true)
    const [generalDraft, setGeneralDraft] = useState<EventDraft>(createBlankEventDraft())
    const editingEventId = searchParams.get('eventId') ?? ''
    const [prefilledEditingEventId, setPrefilledEditingEventId] = useState('')
    const [addressLookupLoading, setAddressLookupLoading] = useState(false)
    const [eventCategoryDraft, setEventCategoryDraft] = useState('')
    const [communityCategoryDraft, setCommunityCategoryDraft] = useState('')
    const [categoryDrafts, setCategoryDrafts] = useState<Record<string, string>>({})
    const [categoryErrors, setCategoryErrors] = useState<{ event: string; community: string }>({
        event: '',
        community: '',
    })
    const [generalEventErrors, setGeneralEventErrors] = useState<EventFormErrors>(createEmptyEventFormErrors())
    const [requestEventErrors, setRequestEventErrors] = useState<Record<string, EventFormErrors>>({})
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
        setReportPage(0)
    }, [reportStatus])

    const normalizedRole = normalizeRole(user?.role)

    const {data: eventToEdit} = useQuery({
        queryKey: ['admin', 'event-edit', editingEventId],
        queryFn: () => getEvent(editingEventId),
        enabled: Boolean(editingEventId) && (normalizedRole === 'ADMIN' || normalizedRole.endsWith('_MANAGER')),
    })

    useEffect(() => {
        if (!editingEventId || !eventToEdit) {
            setPrefilledEditingEventId('')
            setGeneralEventErrors((prev) => {
                if (!prev.startAt) return prev
                const next = { ...prev }
                delete next.startAt
                return next
            })
            return
        }
        if (prefilledEditingEventId === editingEventId) {
            if (new Date(eventToEdit.startAt).getTime() < Date.now()) {
                setGeneralEventErrors((prev) => ({
                    ...prev,
                    startAt: '이미 시작된 행사는 시작일을 바꾸지 말고 종료일만 수정해 주세요.',
                }))
            }
            return
        }
        setGeneralCreateOpen(true)
        setGeneralDraft(buildEventDraftFromEvent(eventToEdit))
        if (new Date(eventToEdit.startAt).getTime() < Date.now()) {
            setGeneralEventErrors((prev) => ({
                ...prev,
                startAt: '이미 시작된 행사는 시작일을 바꾸지 말고 종료일만 수정해 주세요.',
            }))
        } else {
            setGeneralEventErrors((prev) => {
                if (!prev.startAt) return prev
                const next = { ...prev }
                delete next.startAt
                return next
            })
        }
        setPrefilledEditingEventId(editingEventId)
    }, [editingEventId, eventToEdit, prefilledEditingEventId])

    useEffect(() => {
        if (!searchParams.get('tab')) {
            const next = new URLSearchParams(searchParams)
            next.set('tab', 'manage')
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

    const isAdmin = normalizedRole === 'ADMIN'
    const isManager = normalizedRole === 'MANAGER' || normalizedRole.endsWith('_MANAGER')
    const canViewEventRequests = isAdmin || isManager
    const canCreateCategories = normalizedRole === 'ADMIN'
    const canViewOperationRequests = normalizedRole === 'ADMIN'

    const {
        data: adminUsersPage = {content: [], totalElements: 0, totalPages: 0, size: 0, page: 0},
    } = useQuery({
        queryKey: ['admin', 'users', userPage, submittedUserSearchEmail, submittedUserSearchName, submittedUserSearchRole],
        queryFn: () =>
            getAdminUsers({
                page: userPage,
                size: 12,
                email: submittedUserSearchEmail.trim() || undefined,
                name: submittedUserSearchName.trim() || undefined,
                role: submittedUserSearchRole === 'ALL' ? undefined : submittedUserSearchRole,
            }),
        enabled: isAdmin,
    })

    const {data: selectedUserDetail} = useQuery<AdminUserDetailItem>({
        queryKey: ['admin', 'user-detail', selectedUserId],
        queryFn: () => getAdminUserDetail(selectedUserId ?? ''),
        enabled: Boolean(selectedUserId),
    })

    const {
        data: blacklistPageData = {content: [], totalElements: 0, totalPages: 0, size: 0, page: 0},
    } = useQuery<BlacklistPage>({
        queryKey: ['admin', 'blacklists', blacklistPage, blacklistStatus],
        queryFn: () =>
            getBlacklists({
                page: blacklistPage,
                size: 10,
                status: blacklistStatus === 'ALL' ? undefined : blacklistStatus,
            }),
        enabled: isAdmin && activeTab === 'blacklist',
    })

    const {
        data: adminNotificationsPage = {content: [], totalElements: 0, totalPages: 0, size: 0, page: 0},
    } = useQuery({
        queryKey: ['admin', 'notifications', adminNotificationPage],
        queryFn: () => getNotifications({page: adminNotificationPage, size: 10}),
        enabled: isAdmin && activeTab === 'notifications',
    })

    const blacklistUserDetails = useQueries({
        queries: blacklistPageData.content.map((item) => ({
            queryKey: ['admin', 'blacklist-user-detail', item.userId],
            queryFn: () => getAdminUserDetail(item.userId),
            enabled: isAdmin && activeTab === 'blacklist',
        })),
    })

    const enrichedBlacklistRows = useMemo(() => {
        return blacklistPageData.content.map((item, index) => ({
            ...item,
            userDetail: blacklistUserDetails[index]?.data,
        }))
    }, [blacklistPageData.content, blacklistUserDetails])

    const visibleBlacklistRows = useMemo(() => {
        const keyword = blacklistSubmittedSearch.trim().toLowerCase()
        if (!keyword) return enrichedBlacklistRows
        return enrichedBlacklistRows.filter((item) => {
            const detail = item.userDetail
            return [
                item.userId,
                detail?.email,
                detail?.name,
                detail?.nickname,
            ].some((value) => String(value ?? '').toLowerCase().includes(keyword))
        })
    }, [blacklistSubmittedSearch, enrichedBlacklistRows])

    const selectedBlacklistItem = useMemo(
        () => enrichedBlacklistRows.find((item) => item.id === selectedBlacklistId) ?? null,
        [enrichedBlacklistRows, selectedBlacklistId],
    )

    const {data: eventRequests = []} = useQuery({
        queryKey: ['admin', 'event-requests', requestStatus],
        queryFn: () => getEventRequests({size: 12, status: requestStatus === 'ALL' ? undefined : requestStatus}),
        enabled: showLegacyManageSection && activeTab === 'manage' && canViewEventRequests,
    })

    const {data: operationRequests = []} = useQuery({
        queryKey: ['admin', 'operation-requests', operationStatus],
        queryFn: () => getOperationRequests({
            size: 12,
            status: operationStatus === 'ALL' ? undefined : operationStatus
        }),
        enabled: showLegacyManageSection && activeTab === 'manage' && canViewOperationRequests,
    })

    const {
        data: reportPageData = {content: [], totalElements: 0, totalPages: 0, size: 0, page: 0},
    } = useQuery<ReportPage>({
        queryKey: ['admin', 'reports', reportStatus, reportPage],
        queryFn: () => getReports({
            page: reportPage,
            size: 6,
            status: reportStatus === 'ALL' ? undefined : reportStatus,
        }),
        enabled: activeTab === 'reports',
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

    const groupedReports = useMemo(() => {
        const groups = new Map<string, {
            targetId: string
            targetType: string
            targetUserId?: string
            targetContent?: string | null
            reports: ReportItem[]
        }>()

        ;(reportPageData.content as ReportItem[]).forEach((report) => {
            const key = report.targetId ?? report.id
            const current = groups.get(key)
            const nextItem = {
                targetId: report.targetId,
                targetType: report.targetType,
                targetUserId: report.targetUserId,
                targetContent: report.targetContent,
                reports: current ? [...current.reports, report] : [report],
            }
            groups.set(key, nextItem)
        })

        return Array.from(groups.values())
    }, [reportPageData.content])

    const approveMutation = useMutation({
        mutationFn: approveEventRequest,
        onSuccess: () => queryClient.invalidateQueries({queryKey: ['admin', 'event-requests']}),
    })

    const rejectMutation = useMutation({
        mutationFn: ({requestId, rejectReason}: { requestId: string; rejectReason: string }) =>
            rejectEventRequest(requestId, rejectReason),
        onSuccess: () => queryClient.invalidateQueries({queryKey: ['admin', 'event-requests']}),
    })

    const reportBlindMutation = useMutation({
        mutationFn: async ({targetId}: { targetId: string }) => updateAdminChatMessageStatus(targetId, 'BLINDED'),
        onSuccess: async () => {
            await Promise.all([
                queryClient.invalidateQueries({queryKey: ['admin', 'reports']}),
                queryClient.invalidateQueries({queryKey: ['admin', 'messages']}),
            ])
        },
    })

    const reportReviewMutation = useMutation({
        mutationFn: async ({reportIds, status, operatorMemo}: { reportIds: string[]; status: string; operatorMemo: string }) => {
            await Promise.all(reportIds.map((reportId) => updateReportStatus(reportId, status, operatorMemo)))
        },
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: ['admin', 'reports']})
            await queryClient.invalidateQueries({queryKey: ['admin', 'blacklists']})
        },
    })

    const messageMutation = useMutation({
        mutationFn: ({messageId, status}: { messageId: string; status: 'ACTIVE' | 'BLINDED' }) =>
            updateAdminChatMessageStatus(messageId, status),
        onSuccess: () => queryClient.invalidateQueries({queryKey: ['admin', 'messages']}),
    })

    const userRoleMutation = useMutation({
        mutationFn: ({userId, role}: { userId: string; role: string }) => changeAdminUserRole(userId, role),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: ['admin', 'users']})
        },
    })

    const blacklistCreateMutation = useMutation({
        mutationFn: ({userId, reason}: { userId: string; reason: string }) => createBlacklist(userId, reason),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: ['admin', 'blacklists']})
            await queryClient.invalidateQueries({queryKey: ['admin', 'users']})
        },
        onError: (error) => {
            window.alert(getErrorMessage(error, '블랙리스트 등록에 실패했어요.'))
        },
    })

    const blacklistReleaseMutation = useMutation({
        mutationFn: ({blacklistId, reason}: { blacklistId: string; reason: string }) => releaseBlacklist(blacklistId, reason),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: ['admin', 'blacklists']})
            await queryClient.invalidateQueries({queryKey: ['admin', 'users']})
        },
        onError: (error) => {
            window.alert(getErrorMessage(error, '블랙리스트 해제에 실패했어요.'))
        },
    })

    const notificationMarkAllMutation = useMutation({
        mutationFn: markAllNotificationsAsRead,
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: ['admin', 'notifications']})
        },
    })

    const notificationDeleteMutation = useMutation({
        mutationFn: deleteNotification,
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: ['admin', 'notifications']})
        },
    })

    const createEventMutation = useMutation({
        mutationFn: ({requestId, draft}: { requestId?: string; draft: EventDraft }) =>
            createEvent({
                requestId,
                ...buildEventPayload(draft),
            }),
        onSuccess: async (createdEvent, variables) => {
            if (variables?.requestId) {
                setRequestEventErrors((prev) => {
                    const next = {...prev}
                    delete next[variables.requestId ?? '']
                    return next
                })
            } else {
                setGeneralEventErrors(createEmptyEventFormErrors())
            }
            await queryClient.invalidateQueries({queryKey: ['admin', 'event-requests']})
            await queryClient.invalidateQueries({queryKey: ['events']})
            await queryClient.invalidateQueries({queryKey: ['admin', 'chat-rooms']})
            await queryClient.invalidateQueries({queryKey: ['admin', 'event-categories']})
            navigate(`/events/${createdEvent.id}`)
        },
        onError: (error: unknown, variables) => {
            const fieldErrors = mapEventFormErrors(getErrorMessage(error, '행사를 생성하지 못했어요.'))
            if (variables?.requestId) {
                setRequestEventErrors((prev) => ({
                    ...prev,
                    [variables.requestId ?? '']: fieldErrors,
                }))
                return
            }
            setGeneralEventErrors(fieldErrors)
        },
    })

    const saveGeneralEventMutation = useMutation({
        mutationFn: ({draft}: { draft: EventDraft }) => {
            const payload = buildEventPayload(draft)
            if (editingEventId) {
                return updateEvent(editingEventId, payload)
            }
            return createEvent(payload)
        },
        onSuccess: async (savedEvent) => {
            setGeneralEventErrors(createEmptyEventFormErrors())
            await queryClient.invalidateQueries({queryKey: ['events']})
            await queryClient.invalidateQueries({queryKey: ['event', savedEvent.id]})
            await queryClient.invalidateQueries({queryKey: ['admin', 'chat-rooms']})
            await queryClient.invalidateQueries({queryKey: ['admin', 'event-requests']})
            const next = new URLSearchParams(searchParams)
            next.delete('panel')
            next.delete('eventId')
            setSearchParams(next, {replace: true})
            setGeneralCreateOpen(false)
            setPrefilledEditingEventId('')
            navigate(`/events/${savedEvent.id}`)
        },
        onError: (error: unknown) => {
            setGeneralEventErrors(mapEventFormErrors(getErrorMessage(error, '행사를 저장하지 못했어요.')))
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
        next.delete('panel')
        setSearchParams(next, {replace: true})
    }

    const tabs: { value: AdminTab; label: string; description: string }[] = [
        {value: 'manage', label: '행사/카테고리', description: '생성·수정'},
        {value: 'reports', label: '신고', description: '블라인드·처리'},
        {value: 'chat', label: '채팅', description: '메시지·방'},
        {value: 'users', label: '사용자 조회', description: '이메일·이름·권한'},
        {value: 'blacklist', label: '블랙리스트', description: '차단·해제'},
        {value: 'notifications', label: '알림', description: '전체 조회'},
    ]
    const visibleTabs = useMemo(
        () => tabs.filter((tab) => (tab.value === 'users' || tab.value === 'blacklist' || tab.value === 'notifications') ? isAdmin : true),
        [isAdmin],
    )

    return (
        <div className="mx-auto max-w-[1180px] space-y-6 px-5 py-5 md:px-8 md:py-7">
            <section className="rounded-[28px] bg-slate-950 px-5 py-4 text-white shadow-[0_12px_30px_rgba(15,23,42,0.14)] md:px-6 md:py-5">
                <div className="space-y-2.5">
                    <div className="inline-flex rounded-full bg-white/10 px-3 py-1 text-xs font-semibold text-slate-100">
                        관리
                    </div>
                    <div className="text-[22px] font-black tracking-tight text-white md:text-[26px]">
                        운영과 관리를 한 곳에서 빠르게 처리합니다.
                    </div>
                    <div className="rounded-[20px] bg-white/5 p-3 text-xs leading-5 text-slate-300">
                        <div className="text-xs font-semibold text-slate-400">접속 정보</div>
                        <div className="mt-1.5 text-sm leading-6 text-slate-300">
                            {user?.nickname ?? '관리자'}로 접속 중입니다. 권한에 따라 보이는 메뉴가 달라집니다.
                        </div>
                    </div>
                </div>
            </section>

            <section
                className="rounded-[22px] border border-[var(--line)] bg-white p-3 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
                <div className="flex flex-wrap items-center gap-2">
                    {visibleTabs.map((tab) => (
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

            {activeTab === 'manage' && (
                <section className="grid gap-5 lg:grid-cols-[6fr_4fr]">
                    <div className="space-y-4">
                        {generalCreateOpen ? (
                            <div className="max-w-full overflow-x-hidden">
                                <EventCreatePanel
                                    title={editingEventId ? '행사 수정' : '일반 행사 생성'}
                                    subtitle={editingEventId
                                        ? '기존 행사 내용을 불러와서 필요한 부분만 수정할 수 있어요.'
                                        : '관리자/매니저가 직접 전체 항목을 채워서 행사와 채팅방을 함께 만듭니다.'}
                                    draft={generalDraftWithCategory}
                                    setDraft={(patch) => setGeneralDraft((prev) => ({...prev, ...patch}))}
                                    categoryOptions={eventCategories as any[]}
                                    onSubmit={() => {
                                        const errors = validateEventDraft(generalDraftWithCategory)
                                        if (Object.keys(errors).length > 0) {
                                            setGeneralEventErrors(errors)
                                            return
                                        }
                                        saveGeneralEventMutation.mutate({draft: generalDraftWithCategory})
                                    }}
                                    onClose={() => setGeneralCreateOpen(false)}
                                    loading={saveGeneralEventMutation.isPending}
                                    onLookupPlace={() => {
                                        void lookupPlaceForDraft((patch) => setGeneralDraft((prev) => ({...prev, ...patch})))
                                    }}
                                    placeLookupLoading={addressLookupLoading}
                                    onAttachImage={(file) => {
                                        void attachImageForDraft(file, (patch) => setGeneralDraft((prev) => ({...prev, ...patch})))
                                    }}
                                    submitLabel={editingEventId ? '행사 수정' : '행사 등록'}
                                    fieldErrors={generalEventErrors}
                                />
                            </div>
                        ) : (
                            <button
                                type="button"
                                onClick={() => setGeneralCreateOpen(true)}
                                className="w-full rounded-[24px] border border-[var(--line)] bg-white px-4 py-5 text-left text-sm font-semibold text-slate-700 shadow-[0_12px_30px_rgba(15,23,42,0.04)]"
                            >
                                행사 생성 패널 열기
                            </button>
                        )}
                    </div>

                    <div className="space-y-4">
                        {canCreateCategories && (
                            <>
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
                            </>
                        )}
                    </div>
                </section>
            )}

            {showLegacyManageSection && activeTab === 'manage' && (
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
                                                onClick={() => {
                                                    if (editingEventId && generalCreateOpen) {
                                                        const next = new URLSearchParams(searchParams)
                                                        next.delete('eventId')
                                                        next.delete('panel')
                                                        setSearchParams(next, {replace: true})
                                                        setGeneralCreateOpen(false)
                                                        setPrefilledEditingEventId('')
                                                        return
                                                    }
                                                    setGeneralCreateOpen((prev) => !prev)
                                                }}
                                                className="rounded-full border border-[var(--line)] bg-white px-2 py-1 text-[9px] font-semibold text-slate-700 hover:bg-slate-50 md:px-2.5 md:py-1.5 md:text-[10px]"
                                            >
                                                {editingEventId ? '수정 닫기' : generalCreateOpen ? '일반 생성 닫기' : '일반 행사 생성'}
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
                                    <div className="max-w-full overflow-x-hidden">
                                        <EventCreatePanel
                                        title={editingEventId ? '행사 수정' : '일반 행사 생성'}
                                        subtitle={editingEventId
                                            ? '기존 행사 내용을 불러와서 필요한 부분만 수정할 수 있어요.'
                                            : '관리자/매니저가 직접 전체 항목을 채워서 행사와 채팅방을 함께 만듭니다.'}
                                        draft={generalDraftWithCategory}
                                        setDraft={(patch) => setGeneralDraft((prev) => ({...prev, ...patch}))}
                                        categoryOptions={eventCategories as any[]}
                                        onSubmit={() => {
                                            const errors = validateEventDraft(generalDraftWithCategory)
                                            if (Object.keys(errors).length > 0) {
                                                setGeneralEventErrors(errors)
                                                return
                                            }
                                            saveGeneralEventMutation.mutate({draft: generalDraftWithCategory})
                                        }}
                                        onClose={() => {
                                            const next = new URLSearchParams(searchParams)
                                            next.delete('panel')
                                            next.delete('eventId')
                                            setSearchParams(next, {replace: true})
                                            setGeneralCreateOpen(false)
                                            setPrefilledEditingEventId('')
                                        }}
                                        loading={saveGeneralEventMutation.isPending}
                                        onLookupPlace={() => {
                                            void lookupPlaceForDraft((patch) => setGeneralDraft((prev) => ({...prev, ...patch})))
                                        }}
                                        placeLookupLoading={addressLookupLoading}
                                        onAttachImage={(file) => {
                                            void attachImageForDraft(file, (patch) => setGeneralDraft((prev) => ({...prev, ...patch})))
                                        }}
                                        submitLabel={editingEventId ? '행사 수정' : '행사 등록'}
                                        fieldErrors={generalEventErrors}
                                        />
                                    </div>
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
                                                        className="whitespace-pre-line break-words text-xs leading-5 text-slate-600">
                                                        {renderTextWithLinks(request.description)}
                                                    </div>
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
                                                <div className="mt-4">
                                                    <div className="max-w-full overflow-x-hidden">
                                                        <EventCreatePanel
                                                        title="요청 정보로 행사 생성"
                                                        subtitle="요청 글의 내용을 자동으로 이어받아 필요한 항목만 채우면 됩니다."
                                                        draft={requestDrafts[request.id] ?? buildRequestDraft(request, eventCategoryMap)}
                                                        setDraft={(patch) => setRequestDrafts((prev) => mergeDraft(prev, request.id, patch))}
                                                        categoryOptions={eventCategories as any[]}
                                                        onSubmit={() => {
                                                            const currentDraft = requestDrafts[request.id] ?? buildRequestDraft(request, eventCategoryMap)
                                                            const errors = validateEventDraft(currentDraft)
                                                            if (Object.keys(errors).length > 0) {
                                                                setRequestEventErrors((prev) => ({
                                                                    ...prev,
                                                                    [request.id]: errors,
                                                                }))
                                                                return
                                                            }
                                                            createEventMutation.mutate({
                                                                requestId: request.id,
                                                                draft: currentDraft,
                                                            })
                                                        }}
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
                                                        submitLabel="행사 생성"
                                                        fieldErrors={requestEventErrors[request.id] ?? undefined}
                                                        />
                                                    </div>
                                                </div>
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
                                                            className="whitespace-pre-line break-words text-sm leading-6 text-slate-600">{renderTextWithLinks(request.content)}</div>
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
                <section className="space-y-4 rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
                    <SectionHeader
                        title="신고"
                        action={
                            <div className="flex flex-wrap gap-2">
                                {REPORT_STATUS_FILTERS.map((item) => (
                                    <Pill key={item} active={reportStatus === item} onClick={() => setReportStatus(item)}>
                                        {labelReportFilter(item)}
                                    </Pill>
                                ))}
                            </div>
                        }
                    />
                    <div className="text-xs leading-5 text-slate-500">
                        왼쪽은 원문 스냅샷과 처리, 오른쪽은 같은 타겟에 들어온 신고 기록이에요.
                    </div>

                    <div className="space-y-3">
                        {groupedReports.map((group) => {
                            const hasAutoBlinded = group.reports.some((report) => report.status === 'AUTO_BLINDED')
                            const targetStatus = hasAutoBlinded ? 'AUTO_BLINDED' : group.reports[0]?.status ?? 'PENDING'
                            const blindLabel = '채팅 블라인드'
                            const targetUserId = group.targetUserId
                            const targetContent = group.targetContent ?? '원문을 불러오지 못했어요.'
                            const reviewForm = reportReviewForms[group.targetId] ?? {status: 'RESOLVED', operatorMemo: ''}
                            const reporterSystemId = '00000000-0000-0000-0000-000000000000'

                            return (
                                <section
                                    key={group.targetId}
                                    className={`rounded-[22px] border border-[var(--line)] bg-slate-50 p-3 ${hasAutoBlinded ? 'ring-1 ring-rose-200' : ''}`}
                                >
                                    <div className="grid gap-3 lg:grid-cols-[minmax(280px,320px)_minmax(0,1fr)]">
                                        <article className="rounded-[18px] border border-[var(--line)] bg-white p-3">
                                            <div className="flex flex-wrap items-center gap-2">
                                                <span className={`rounded-full px-2.5 py-1 text-[11px] font-semibold ${
                                                    hasAutoBlinded ? 'bg-rose-100 text-rose-700' : 'bg-slate-100 text-slate-600'
                                                }`}>
                                                    {hasAutoBlinded ? labelReportStatus('AUTO_BLINDED') : '묶음'}
                                                </span>
                                                <span className="rounded-full bg-sky-50 px-2.5 py-1 text-[11px] font-semibold text-sky-700">
                                                    {labelReportTargetType(group.targetType)}
                                                </span>
                                                <span className={`rounded-full px-2.5 py-1 text-[11px] font-semibold ${
                                                    targetStatus === 'AUTO_BLINDED'
                                                        ? 'bg-rose-100 text-rose-700'
                                                        : targetStatus === 'RESOLVED'
                                                            ? 'bg-emerald-100 text-emerald-700'
                                                            : targetStatus === 'REJECTED'
                                                                ? 'bg-slate-200 text-slate-700'
                                                                : 'bg-slate-100 text-slate-600'
                                                }`}>
                                                    {labelReportStatus(targetStatus)}
                                                </span>
                                                <span className="text-[11px] text-slate-500">{group.reports.length}건</span>
                                            </div>

                                            <div className="mt-3 rounded-[16px] border border-[var(--line)] bg-slate-50 px-3 py-3">
                                                <div className="text-[11px] font-bold text-slate-700">타겟 카드</div>
                                                <div className="mt-2 whitespace-pre-wrap text-sm leading-6 text-slate-700">
                                                    {targetContent}
                                                </div>
                                                <div className="mt-3 grid gap-1 text-[11px] text-slate-500">
                                                    <div className="flex flex-col gap-0.5">
                                                        <span>타겟 유저</span>
                                                        <span className="break-all font-semibold text-slate-700">{targetUserId ?? '정보 없음'}</span>
                                                    </div>
                                                    <div className="flex flex-col gap-0.5">
                                                        <span>타겟 ID</span>
                                                        <span className="break-all font-semibold text-slate-700">{group.targetId ?? '정보 없음'}</span>
                                                    </div>
                                                </div>
                                            </div>

                                            <div className="mt-3 flex flex-wrap gap-2">
                                                {group.targetType === 'CHAT' && (
                                                    <button
                                                        type="button"
                                                        disabled={reportBlindMutation.isPending || !group.targetId}
                                                        onClick={() => reportBlindMutation.mutate({targetId: group.targetId})}
                                                        className="rounded-full bg-slate-950 px-3 py-2 text-xs font-semibold text-white disabled:opacity-70"
                                                    >
                                                        {blindLabel}
                                                    </button>
                                                )}
                                                <button
                                                    type="button"
                                                    disabled={!targetUserId}
                                                    onClick={() => {
                                                        const reason = window.prompt('블랙리스트 등록 사유를 입력하세요.')
                                                        if (!reason?.trim() || !targetUserId) return
                                                        blacklistCreateMutation.mutate({userId: targetUserId, reason: reason.trim()})
                                                    }}
                                                    className="rounded-full border border-rose-200 bg-rose-50 px-3 py-2 text-xs font-semibold text-rose-700 disabled:opacity-60"
                                                >
                                                    블랙리스트 등록
                                                </button>
                                            </div>

                                            {hasAutoBlinded && (
                                                <div className="mt-3 rounded-[16px] border border-[var(--line)] bg-white p-3">
                                                    <div className="text-[11px] font-semibold text-slate-500">검토 처리</div>
                                                    <div className="mt-2 space-y-2">
                                                        <select
                                                            value={reviewForm.status}
                                                            onChange={(e) => setReportReviewForms((prev) => ({
                                                                ...prev,
                                                                [group.targetId]: {...reviewForm, status: e.target.value},
                                                            }))}
                                                            className="w-full rounded-full border border-[var(--line)] bg-slate-50 px-3 py-2 text-xs outline-none"
                                                        >
                                                            <option value="RESOLVED">RESOLVED</option>
                                                            <option value="REJECTED">REJECTED</option>
                                                        </select>
                                                        <input
                                                            value={reviewForm.operatorMemo}
                                                            onChange={(e) => setReportReviewForms((prev) => ({
                                                                ...prev,
                                                                [group.targetId]: {...reviewForm, operatorMemo: e.target.value},
                                                            }))}
                                                            placeholder="설명 / 메모"
                                                            className="w-full rounded-full border border-[var(--line)] bg-slate-50 px-3 py-2 text-xs outline-none"
                                                        />
                                                        <button
                                                            type="button"
                                                            onClick={() => reportReviewMutation.mutate({
                                                                reportIds: group.reports.map((report) => report.id),
                                                                status: reviewForm.status,
                                                                operatorMemo: reviewForm.operatorMemo.trim() || '처리 완료',
                                                            })}
                                                            className="w-full rounded-full bg-[var(--accent)] px-3 py-2 text-xs font-semibold text-white"
                                                        >
                                                            저장
                                                        </button>
                                                    </div>
                                                </div>
                                            )}
                                        </article>

                                        <div className="min-w-0">
                                            <div className="mb-2 flex items-center justify-between gap-2">
                                                <div className="text-[11px] font-semibold text-slate-500">
                                                    신고 {group.reports.length}건
                                                </div>
                                                <div className="text-[11px] text-slate-400">
                                                    타겟별 신고 목록
                                                </div>
                                            </div>
                                            <div className="overflow-x-auto pb-2">
                                                <div className="flex min-w-max gap-3">
                                                    {group.reports.map((report) => {
                                                        const reporterChipClass = report.reporterType === 'SYSTEM_AI'
                                                            ? 'bg-sky-100 text-sky-700'
                                                            : 'bg-emerald-100 text-emerald-700'
                                                        const statusChipClass = report.status === 'AUTO_BLINDED'
                                                            ? 'bg-rose-100 text-rose-700'
                                                            : report.status === 'RESOLVED'
                                                                ? 'bg-emerald-100 text-emerald-700'
                                                                : 'bg-slate-100 text-slate-600'

                                                        return (
                                                            <article
                                                                key={report.id}
                                                                className={`shrink-0 rounded-[18px] border border-[var(--line)] bg-white p-3 ${report.status === 'AUTO_BLINDED' ? 'w-[320px]' : 'w-[290px]'}`}
                                                            >
                                                                <div className="flex flex-wrap items-center gap-2">
                                                                    <span className={`rounded-full px-2.5 py-1 text-[11px] font-semibold ${statusChipClass}`}>
                                                                        {labelReportStatus(report.status)}
                                                                    </span>
                                                                    <span className={`rounded-full px-2.5 py-1 text-[11px] font-semibold ${reporterChipClass}`}>
                                                                        {labelReporterType(report.reporterType)}
                                                                    </span>
                                                                    <span className="rounded-full bg-slate-50 px-2.5 py-1 text-[11px] font-semibold text-slate-600">
                                                                        {report.category}
                                                                    </span>
                                                                </div>

                                                                <div className="mt-3 space-y-1">
                                                                    <div className="text-sm font-semibold text-slate-950">
                                                                        {report.description}
                                                                    </div>
                                                                    <div className="text-[11px] text-slate-500">
                                                                        리포터{' '}
                                                                        <span className="font-semibold text-slate-700">
                                                                            {report.reporterId === reporterSystemId ? 'SYSTEM' : report.reporterId}
                                                                        </span>
                                                                    </div>
                                                                    <div className="text-[11px] text-slate-500">
                                                                        신고 타입{' '}
                                                                        <span className="font-semibold text-slate-700">
                                                                            {labelReportTargetType(report.targetType)}
                                                                        </span>
                                                                    </div>
                                                                </div>

                                                                <div className="mt-3 grid gap-1.5 text-[11px] text-slate-500">
                                                                    <div className="flex flex-col gap-0.5">
                                                                        <span>타겟 ID</span>
                                                                        <span className="break-all font-semibold text-slate-700">{report.targetId}</span>
                                                                    </div>
                                                                </div>
                                                            </article>
                                                        )
                                                    })}
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </section>
                            )
                        })}
                        {groupedReports.length === 0 && <EmptyState text="신고 목록이 비어 있어요."/>}
                    </div>

                    <div className="flex flex-wrap items-center justify-between gap-3 border-t border-[var(--line)] pt-3">
                        <div className="text-xs text-slate-500">
                            총 {reportPageData.totalElements}건
                        </div>
                        <div className="flex flex-wrap items-center gap-2">
                            <button
                                type="button"
                                disabled={reportPageData.page === 0}
                                onClick={() => setReportPage((prev) => Math.max(prev - 1, 0))}
                                className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-600 disabled:opacity-60"
                            >
                                이전
                            </button>
                            {Array.from({length: Math.max(reportPageData.totalPages || 0, 1)}, (_, index) => index).slice(0, 5).map((pageIndex) => (
                                <button
                                    key={pageIndex}
                                    type="button"
                                    onClick={() => setReportPage(pageIndex)}
                                    className={`rounded-full px-3 py-1.5 text-xs font-semibold ${
                                        reportPageData.page === pageIndex ? 'bg-[var(--accent-soft)] text-[var(--accent)]' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                                    }`}
                                >
                                    {pageIndex + 1}
                                </button>
                            ))}
                            <button
                                type="button"
                                disabled={reportPageData.page + 1 >= Math.max(reportPageData.totalPages || 0, 1)}
                                onClick={() => setReportPage((prev) => prev + 1)}
                                className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-600 disabled:opacity-60"
                            >
                                다음
                            </button>
                        </div>
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

            {activeTab === 'users' && isAdmin && (
                <section className="space-y-4 rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
                    <SectionHeader
                        title="사용자 조회"
                        action={<span className="text-xs text-slate-500">{adminUsersPage.totalElements}명</span>}
                    />
                    <p className="text-sm leading-6 text-slate-600">
                        이메일, 이름은 각각 부분 일치로 찾을 수 있어요. 권한만으로도 조회할 수 있고, 조건이 없으면 전체 목록으로 보여줘요.
                    </p>

                    <div className="rounded-[20px] border border-[var(--line)] bg-slate-50 p-4">
                        <div className="grid gap-2.5 lg:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_minmax(132px,0.72fr)_auto_auto] lg:items-end">
                            <label className="grid gap-1">
                                <span className="text-[11px] font-semibold text-slate-500">이메일</span>
                                <input
                                    value={userSearchEmail}
                                    onChange={(e) => {
                                        setUserSearchEmail(e.target.value)
                                    }}
                                    placeholder="이메일"
                                    className="h-[42px] rounded-full border border-[var(--line)] bg-white px-3 py-2.5 text-sm outline-none"
                                />
                            </label>
                            <label className="grid gap-1">
                                <span className="text-[11px] font-semibold text-slate-500">이름</span>
                                <input
                                    value={userSearchName}
                                    onChange={(e) => {
                                        setUserSearchName(e.target.value)
                                    }}
                                    placeholder="이름"
                                    className="h-[42px] rounded-full border border-[var(--line)] bg-white px-3 py-2.5 text-sm outline-none"
                                />
                            </label>
                            <select
                                value={userSearchRole}
                                onChange={(e) => {
                                    setUserSearchRole(e.target.value as typeof USER_ROLE_FILTERS[number])
                                }}
                                className="h-[42px] rounded-full border border-[var(--line)] bg-white px-3 py-2.5 text-sm outline-none"
                            >
                                {USER_ROLE_FILTERS.map((role) => (
                                    <option key={role} value={role}>
                                        {role === 'ALL' ? '권한 전체' : labelUserRole(role)}
                                    </option>
                                ))}
                            </select>
                            <button
                                type="button"
                                onClick={() => {
                                    setUserSearchEmail('')
                                    setUserSearchName('')
                                    setUserSearchRole('ALL')
                                    setSubmittedUserSearchEmail('')
                                    setSubmittedUserSearchName('')
                                    setSubmittedUserSearchRole('ALL')
                                    setUserPage(0)
                                }}
                                className="h-[42px] self-end rounded-full border border-[var(--line)] bg-white px-3 py-2.5 text-sm font-semibold text-slate-700"
                            >
                                초기화
                            </button>
                            <button
                                type="button"
                                onClick={() => {
                                    setSubmittedUserSearchEmail(userSearchEmail)
                                    setSubmittedUserSearchName(userSearchName)
                                    setSubmittedUserSearchRole(userSearchRole)
                                    setUserPage(0)
                                }}
                                className="h-[42px] self-end rounded-full bg-[var(--accent)] px-3 py-2.5 text-sm font-semibold text-white"
                            >
                                검색
                            </button>
                        </div>
                    </div>

                    <div className="hidden rounded-[18px] border border-[var(--line)] bg-white px-4 py-3 text-[11px] font-semibold text-slate-500 lg:grid lg:grid-cols-[110px_minmax(0,0.78fr)_minmax(0,0.82fr)_minmax(0,1.12fr)_minmax(0,1.18fr)_minmax(0,1fr)] lg:gap-3">
                        <div>상태</div>
                        <div>이름</div>
                        <div>닉네임</div>
                        <div>이메일</div>
                        <div>권한변경</div>
                        <div>블랙리스트등록</div>
                    </div>

                    <div className="space-y-3">
                        {adminUsersPage.content.map((item: AdminUserItem) => (
                            <UserRow
                                key={item.userId}
                                user={item}
                                onSelect={() => setSelectedUserId(item.userId)}
                                onChangeRole={(role) => userRoleMutation.mutate({userId: item.userId, role})}
                                onBlacklist={() => {
                                    const reason = window.prompt(`블랙리스트 등록 사유를 입력해주세요.\n대상: ${item.name} (${item.email})`)
                                    if (!reason?.trim()) return
                                    blacklistCreateMutation.mutate({userId: item.userId, reason: reason.trim()})
                                }}
                                loading={userRoleMutation.isPending}
                            />
                        ))}
                        {adminUsersPage.content.length === 0 && <EmptyState text="사용자가 없습니다."/>}
                    </div>

                    <div className="flex items-center justify-center gap-2 pt-2">
                        <button
                            type="button"
                            disabled={adminUsersPage.page === 0}
                            onClick={() => setUserPage((prev) => Math.max(prev - 1, 0))}
                            className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:opacity-40"
                        >
                            이전
                        </button>
                        <div className="flex items-center gap-1">
                            {Array.from({length: Math.max(adminUsersPage.totalPages || 0, 1)}, (_, index) => index).map((pageIndex) => (
                                <button
                                    key={pageIndex}
                                    type="button"
                                    onClick={() => setUserPage(pageIndex)}
                                    className={`h-8 min-w-8 rounded-full px-2 text-xs font-semibold transition-colors ${
                                        adminUsersPage.page === pageIndex ? 'bg-[var(--accent-soft)] text-[var(--accent)]' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                                    }`}
                                >
                                    {pageIndex + 1}
                                </button>
                            ))}
                        </div>
                        <button
                            type="button"
                            disabled={adminUsersPage.page + 1 >= Math.max(adminUsersPage.totalPages || 0, 1)}
                            onClick={() => setUserPage((prev) => prev + 1)}
                            className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:opacity-40"
                        >
                            다음
                        </button>
                    </div>
                </section>
            )}

            {activeTab === 'blacklist' && isAdmin && (
                <section className="space-y-4 rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
                    <SectionHeader
                        title="블랙리스트"
                        action={<span className="text-xs text-slate-500">{blacklistPageData.totalElements}명</span>}
                    />
                    <p className="text-sm leading-6 text-slate-600">
                        블랙리스트 사용자 목록을 확인하고, 상태를 필터링하거나 해제할 수 있어요.
                    </p>

                    <div className="rounded-[20px] border border-[var(--line)] bg-slate-50 p-4">
                        <div className="grid gap-3 lg:grid-cols-[1fr_0.7fr_auto_auto]">
                            <input
                                value={blacklistSearch}
                                onChange={(e) => setBlacklistSearch(e.target.value)}
                                placeholder="UUID / 이름 / 이메일 검색"
                                className="rounded-full border border-[var(--line)] bg-white px-4 py-3 text-sm outline-none"
                            />
                            <select
                                value={blacklistStatus}
                                onChange={(e) => setBlacklistStatus(e.target.value as 'ALL' | BlacklistStatus)}
                                className="rounded-full border border-[var(--line)] bg-white px-4 py-3 text-sm outline-none"
                            >
                                <option value="ALL">상태 전체</option>
                                <option value="ACTIVE">차단 중</option>
                                <option value="INACTIVE">차단 해제됨</option>
                            </select>
                            <button
                                type="button"
                                onClick={() => {
                                    setBlacklistSearch('')
                                    setBlacklistSubmittedSearch('')
                                    setBlacklistStatus('ALL')
                                    setBlacklistPage(0)
                                }}
                                className="rounded-full border border-[var(--line)] bg-white px-4 py-3 text-sm font-semibold text-slate-700"
                            >
                                초기화
                            </button>
                            <button
                                type="button"
                                onClick={() => {
                                    setBlacklistSubmittedSearch(blacklistSearch)
                                    setBlacklistPage(0)
                                }}
                                className="rounded-full bg-[var(--accent)] px-4 py-3 text-sm font-semibold text-white"
                            >
                                검색
                            </button>
                        </div>
                    </div>

                    <div className="hidden rounded-[18px] border border-[var(--line)] bg-white px-4 py-3 text-[11px] font-semibold text-slate-500 lg:grid lg:grid-cols-[120px_1.1fr_1fr_1fr_1.4fr_180px] lg:gap-3">
                        <div>상태</div>
                        <div>UUID</div>
                        <div>이름</div>
                        <div>닉네임</div>
                        <div>이메일</div>
                        <div>처리</div>
                    </div>

                    <div className="space-y-3">
                        {visibleBlacklistRows.map((item) => (
                            <div
                                key={item.id}
                                role="button"
                                tabIndex={0}
                                onClick={() => setSelectedBlacklistId(item.id)}
                                onKeyDown={(e) => {
                                    if (e.key === 'Enter' || e.key === ' ') setSelectedBlacklistId(item.id)
                                }}
                                className="w-full cursor-pointer rounded-[20px] border border-[var(--line)] bg-slate-50 px-4 py-3 text-left transition-colors hover:bg-slate-100/80"
                            >
                                <div className="grid gap-3 lg:grid-cols-[120px_1.1fr_1fr_1fr_1.4fr_180px] lg:items-center">
                                    <div>
                                        <span className={`inline-flex rounded-full px-2.5 py-1 text-[11px] font-semibold ${
                                            item.status === 'ACTIVE'
                                                ? 'bg-rose-100 text-rose-700'
                                                : 'bg-slate-100 text-slate-600'
                                        }`}>
                                            {labelBlacklistStatus(item.status)}
                                        </span>
                                    </div>
                                    <div className="min-w-0 text-sm font-semibold text-slate-950">
                                        <span className="block truncate">{item.userId}</span>
                                    </div>
                                    <div className="min-w-0 text-sm text-slate-700">
                                        <span className="block truncate">{item.userDetail?.name ?? '불러오는 중...'}</span>
                                    </div>
                                    <div className="min-w-0 text-sm text-slate-700">
                                        <span className="block truncate">{item.userDetail?.nickname ?? '불러오는 중...'}</span>
                                    </div>
                                    <div className="min-w-0 text-sm text-slate-600">
                                        <span className="block truncate">{item.userDetail?.email ?? '불러오는 중...'}</span>
                                    </div>
                                    <div className="flex justify-start" onClick={(e) => e.stopPropagation()}>
                                        <button
                                            type="button"
                                            disabled={item.status !== 'ACTIVE' || blacklistReleaseMutation.isPending}
                                            onClick={() => {
                                                const reason = window.prompt(`블랙리스트 해제 사유를 입력해주세요.\n대상: ${item.userDetail?.name ?? item.userId}`)
                                                if (!reason?.trim()) return
                                                blacklistReleaseMutation.mutate({blacklistId: item.id, reason: reason.trim()})
                                            }}
                                            className="rounded-full border border-rose-200 bg-white px-4 py-2 text-sm font-semibold text-rose-700 disabled:opacity-40"
                                        >
                                            해제
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
                        {visibleBlacklistRows.length === 0 && <EmptyState text="블랙리스트 사용자가 없습니다."/>}
                    </div>

                    <div className="flex items-center justify-center gap-2 pt-2">
                        <button
                            type="button"
                            disabled={blacklistPageData.page === 0}
                            onClick={() => setBlacklistPage((prev) => Math.max(prev - 1, 0))}
                            className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:opacity-40"
                        >
                            이전
                        </button>
                        <div className="flex items-center gap-1">
                            {Array.from({length: Math.max(blacklistPageData.totalPages || 0, 1)}, (_, index) => index).map((pageIndex) => (
                                <button
                                    key={pageIndex}
                                    type="button"
                                    onClick={() => setBlacklistPage(pageIndex)}
                                    className={`h-8 min-w-8 rounded-full px-2 text-xs font-semibold transition-colors ${
                                        blacklistPageData.page === pageIndex ? 'bg-[var(--accent-soft)] text-[var(--accent)]' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                                    }`}
                                >
                                    {pageIndex + 1}
                                </button>
                            ))}
                        </div>
                        <button
                            type="button"
                            disabled={blacklistPageData.page + 1 >= Math.max(blacklistPageData.totalPages || 0, 1)}
                            onClick={() => setBlacklistPage((prev) => prev + 1)}
                            className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:opacity-40"
                        >
                            다음
                        </button>
                    </div>
                </section>
            )}

            {activeTab === 'notifications' && isAdmin && (
                <section className="space-y-4 rounded-[24px] border border-[var(--line)] bg-white p-5 shadow-[0_12px_30px_rgba(15,23,42,0.04)]">
                    <SectionHeader
                        title="알림"
                        action={
                            <div className="flex items-center gap-2">
                                <span className="text-xs text-slate-500">{adminNotificationsPage.totalElements}건</span>
                                <button
                                    type="button"
                                    disabled={adminNotificationsPage.content.every((item) => item.readAt !== null) || notificationMarkAllMutation.isPending}
                                    onClick={() => notificationMarkAllMutation.mutate()}
                                    className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:opacity-40"
                                >
                                    모두 읽음
                                </button>
                            </div>
                        }
                    />
                    <p className="text-sm leading-6 text-slate-600">
                        실시간으로 들어온 알림과 DB에 저장된 알림을 함께 확인할 수 있어요.
                    </p>

                    <div className="space-y-3">
                        {adminNotificationsPage.content.map((notification) => (
                            <article
                                key={notification.id}
                                className={`rounded-[18px] border p-3 shadow-sm transition-colors ${
                                    notification.readAt
                                        ? 'border-slate-100 bg-white'
                                        : 'border-[var(--accent-soft)]/60 bg-[var(--accent-soft)]/35'
                                }`}
                            >
                                <div className="flex items-start justify-between gap-3">
                                    <div className="min-w-0 flex-1">
                                        <div className="flex items-center gap-2">
                                            <span className="inline-flex rounded-full bg-slate-100 px-2.5 py-1 text-[10px] font-semibold text-slate-600">
                                                {notification.readAt ? '읽음' : '새 알림'}
                                            </span>
                                        </div>
                                        <div className="mt-2 text-[13px] font-semibold leading-5 text-slate-950">
                                            {notification.title}
                                        </div>
                                        <div className="mt-1 text-[12px] leading-5 text-slate-600">
                                            {notification.content}
                                        </div>
                                    </div>
                                    <button
                                        type="button"
                                        onClick={() => notificationDeleteMutation.mutate(notification.id)}
                                        className="rounded-full border border-slate-200 bg-white px-2.5 py-1.5 text-[11px] font-semibold text-slate-500 transition-colors hover:border-rose-200 hover:text-rose-600"
                                        aria-label="알림 삭제"
                                    >
                                        삭제
                                    </button>
                                </div>
                            </article>
                        ))}
                        {adminNotificationsPage.content.length === 0 && <EmptyState text="알림이 없습니다."/>}
                    </div>

                    <div className="flex items-center justify-center gap-2 pt-2">
                        <button
                            type="button"
                            disabled={adminNotificationsPage.page === 0}
                            onClick={() => setAdminNotificationPage((prev) => Math.max(prev - 1, 0))}
                            className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:opacity-40"
                        >
                            이전
                        </button>
                        <div className="flex items-center gap-1">
                            {Array.from({length: Math.max(adminNotificationsPage.totalPages || 0, 1)}, (_, index) => index).map((pageIndex) => (
                                <button
                                    key={pageIndex}
                                    type="button"
                                    onClick={() => setAdminNotificationPage(pageIndex)}
                                    className={`h-8 min-w-8 rounded-full px-2 text-xs font-semibold transition-colors ${
                                        adminNotificationsPage.page === pageIndex ? 'bg-[var(--accent-soft)] text-[var(--accent)]' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                                    }`}
                                >
                                    {pageIndex + 1}
                                </button>
                            ))}
                        </div>
                        <button
                            type="button"
                            disabled={adminNotificationsPage.page + 1 >= Math.max(adminNotificationsPage.totalPages || 0, 1)}
                            onClick={() => setAdminNotificationPage((prev) => prev + 1)}
                            className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:opacity-40"
                        >
                            다음
                        </button>
                    </div>
                </section>
            )}

            {selectedUserId && (
                <div
                    className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/50 px-4 py-8"
                    onClick={() => setSelectedUserId(null)}
                >
                    <div
                        className="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-[28px] bg-white p-5 shadow-[0_24px_60px_rgba(15,23,42,0.24)] md:p-6"
                        onClick={(e) => e.stopPropagation()}
                    >
                        <div className="flex items-start justify-between gap-3">
                            <div>
                                <div className="inline-flex rounded-full bg-[var(--accent-soft)] px-3 py-1 text-xs font-semibold text-[var(--accent)]">
                                    사용자 상세
                                </div>
                                <div className="mt-2 text-[22px] font-black tracking-tight text-slate-950">
                                    {selectedUserDetail?.name ?? '불러오는 중...'}
                                </div>
                                <div className="mt-1 text-sm text-slate-500">
                                    {selectedUserDetail?.nickname ?? ''}{selectedUserDetail?.nickname ? ' · ' : ''}{selectedUserDetail?.email ?? ''}
                                </div>
                            </div>
                            <button
                                type="button"
                                onClick={() => setSelectedUserId(null)}
                                className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-600"
                            >
                                닫기
                            </button>
                        </div>

                        <div className="mt-5 grid gap-3 md:grid-cols-2">
                            <DetailField label="UUID" value={selectedUserDetail?.userId ?? '정보 없음'} />
                            <DetailField label="이름" value={selectedUserDetail?.name ?? '정보 없음'} />
                            <DetailField label="상태" value={labelUserStatus(selectedUserDetail?.status ?? '')} />
                            <DetailField label="닉네임" value={selectedUserDetail?.nickname ?? '정보 없음'} />
                            <DetailField label="이메일" value={selectedUserDetail?.email ?? '정보 없음'} />
                            <DetailField label="권한" value={labelUserRole(selectedUserDetail?.role ?? '')} />
                            <DetailField label="생성일" value={formatDateTime(selectedUserDetail?.createdAt)} />
                            <DetailField label="수정일" value={formatDateTime(selectedUserDetail?.updatedAt)} />
                        </div>

                        <div className="mt-5 rounded-[20px] border border-[var(--line)] bg-slate-50 p-4">
                            <div className="text-[11px] font-semibold text-slate-500">전화번호</div>
                            <div className="mt-1 text-sm font-medium text-slate-800">
                                {selectedUserDetail?.phoneNumber ?? '정보 없음'}
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {selectedBlacklistItem && (
                <div
                    className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/50 px-4 py-8"
                    onClick={() => setSelectedBlacklistId(null)}
                >
                    <div
                        className="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-[28px] bg-white p-5 shadow-[0_24px_60px_rgba(15,23,42,0.24)] md:p-6"
                        onClick={(e) => e.stopPropagation()}
                    >
                        <div className="flex items-start justify-between gap-3">
                            <div>
                                <div className="inline-flex rounded-full bg-[var(--accent-soft)] px-3 py-1 text-xs font-semibold text-[var(--accent)]">
                                    블랙리스트 상세
                                </div>
                                <div className="mt-2 text-[22px] font-black tracking-tight text-slate-950">
                                    {selectedBlacklistItem.userDetail?.name ?? selectedBlacklistItem.userId}
                                </div>
                                <div className="mt-1 text-sm text-slate-500">
                                    {selectedBlacklistItem.userDetail?.nickname ?? '정보 없음'} · {selectedBlacklistItem.userDetail?.email ?? '정보 없음'}
                                </div>
                            </div>
                            <button
                                type="button"
                                onClick={() => setSelectedBlacklistId(null)}
                                className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-600"
                            >
                                닫기
                            </button>
                        </div>

                        <div className="mt-5 grid gap-3 md:grid-cols-2">
                            <DetailField label="UUID" value={selectedBlacklistItem.userId} />
                            <DetailField label="블랙리스트 상태" value={labelBlacklistStatus(selectedBlacklistItem.status)} />
                            <DetailField label="이름" value={selectedBlacklistItem.userDetail?.name ?? '정보 없음'} />
                            <DetailField label="닉네임" value={selectedBlacklistItem.userDetail?.nickname ?? '정보 없음'} />
                            <DetailField label="이메일" value={selectedBlacklistItem.userDetail?.email ?? '정보 없음'} />
                            <DetailField label="아이디 상태" value={labelUserStatus(selectedBlacklistItem.userDetail?.status ?? '')} />
                        </div>

                        <div className="mt-5 flex items-center justify-end gap-2">
                            <button
                                type="button"
                                onClick={() => {
                                    const reason = window.prompt(`블랙리스트 해제 사유를 입력해주세요.\n대상: ${selectedBlacklistItem.userDetail?.name ?? selectedBlacklistItem.userId}`)
                                    if (!reason?.trim()) return
                                    blacklistReleaseMutation.mutate({blacklistId: selectedBlacklistItem.id, reason: reason.trim()})
                                    setSelectedBlacklistId(null)
                                }}
                                disabled={selectedBlacklistItem.status !== 'ACTIVE'}
                                className="rounded-full border border-rose-200 bg-rose-50 px-4 py-2 text-sm font-semibold text-rose-700 disabled:opacity-40"
                            >
                                해제
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}

function renderTextWithLinks(text: string) {
    const value = String(text ?? '')
    const lines = value.split('\n')
    return lines.map((line, lineIndex) => {
        const parts = line.split(/(https?:\/\/[^\s]+)/g)
        return (
            <span key={`line-${lineIndex}`}>
                {parts.map((part, partIndex) => {
                    const isUrl = /^https?:\/\/[^\s]+$/.test(part)
                    return isUrl ? (
                        <a
                            key={`part-${lineIndex}-${partIndex}`}
                            href={part}
                            target="_blank"
                            rel="noreferrer"
                            className="break-all text-[var(--accent)] underline decoration-[var(--accent-soft)] decoration-2 underline-offset-4 transition-colors hover:text-[var(--accent-dark)]"
                        >
                            {part}
                        </a>
                    ) : (
                        <span key={`part-${lineIndex}-${partIndex}`}>{part}</span>
                    )
                })}
                {lineIndex < lines.length - 1 ? <br /> : null}
            </span>
        )
    })
}

function UserRow({
  user,
  onSelect,
  onChangeRole,
  onBlacklist,
  loading,
}: {
  user: AdminUserItem
  onSelect: () => void
  onChangeRole: (role: AdminUserRole) => void
  onBlacklist: () => void
  loading?: boolean
}) {
  const [role, setRole] = useState<AdminUserRole>(user.role)

  useEffect(() => {
    setRole(user.role)
  }, [user.role])

  return (
    <div
      role="button"
      tabIndex={0}
      onClick={onSelect}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') onSelect()
      }}
      className="w-full cursor-pointer rounded-[20px] border border-[var(--line)] bg-slate-50 px-4 py-3 text-left transition-colors hover:bg-slate-100/80"
    >
      <div className="grid gap-3 lg:grid-cols-[110px_minmax(0,0.78fr)_minmax(0,0.82fr)_minmax(0,1.12fr)_minmax(0,1.18fr)_minmax(0,1fr)] lg:items-center">
        <div className="flex items-center gap-2 lg:block">
          <span className={`inline-flex rounded-full px-2.5 py-1 text-[11px] font-semibold ${
              user.status === 'BLOCKED'
                  ? 'bg-rose-100 text-rose-700'
                  : 'bg-[var(--accent-soft)] text-[var(--accent)]'
          }`}>
            {labelUserStatus(user.status)}
          </span>
        </div>
        <div className="min-w-0 text-sm font-semibold text-slate-950">
          <span className="block truncate">{user.name}</span>
        </div>
        <div className="min-w-0 text-sm text-slate-700">
          <span className="block truncate">{user.nickname}</span>
        </div>
        <div className="min-w-0 text-sm text-slate-600">
          <span className="block truncate">{user.email}</span>
        </div>
        <div className="min-w-0" onClick={(e) => e.stopPropagation()}>
          <select
            value={role}
            onChange={(e) => setRole(e.target.value as AdminUserRole)}
            className="w-full rounded-full border border-[var(--line)] bg-white px-3 py-2 text-sm outline-none"
          >
            {USER_ROLE_OPTIONS.filter((item) => item.value !== 'ALL').map((item) => (
              <option key={item.value} value={item.value}>
                {item.label}
              </option>
            ))}
          </select>
        </div>
        <div className="flex items-center justify-end gap-2 whitespace-nowrap" onClick={(e) => e.stopPropagation()}>
          <button
            type="button"
            disabled={loading || role === user.role}
            onClick={() => onChangeRole(role)}
            className="shrink-0 rounded-full bg-[var(--accent)] px-2.5 py-2 text-[11px] font-semibold text-white disabled:opacity-70"
          >
            {loading ? '저장 중...' : '권한 저장'}
          </button>
          <button
            type="button"
            onClick={onBlacklist}
            className="shrink-0 rounded-full border border-rose-200 bg-rose-50 px-2.5 py-2 text-[11px] font-semibold text-rose-700 hover:bg-rose-100"
          >
            블랙리스트 등록
          </button>
        </div>
      </div>
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

function buildEventDraftFromEvent(event: Event): EventDraft {
    return {
        eventName: event.name ?? '',
        categoryId: event.categoryId ?? '',
        startAt: toDateTimeLocalInput(event.startAt),
        endAt: toDateTimeLocalInput(event.endAt),
        place: event.place ?? '',
        region: event.region ?? '',
        latitude: event.latitude != null ? String(event.latitude) : '',
        longitude: event.longitude != null ? String(event.longitude) : '',
        radius: event.radius != null ? String(event.radius) : '',
        minFee: event.minFee != null ? String(event.minFee) : '',
        maxFee: event.maxFee != null ? String(event.maxFee) : '',
        hasTicketing: Boolean(event.hasTicketing),
        ticketingOpenAt: toDateTimeLocalInput(event.ticketingOpenAt),
        ticketingCloseAt: toDateTimeLocalInput(event.ticketingCloseAt),
        ticketingLink: event.ticketingLink ?? '',
        officialLink: event.officialLink ?? '',
        performer: event.performer ?? '',
        description: event.description ?? '',
        img: event.img ?? '',
        imgFileName: '',
        schedules: (event.schedules ?? []).map((schedule) => ({
            name: schedule.name ?? schedule.title ?? schedule.memo ?? '',
            startAt: toDateTimeLocalInput(schedule.startTime ?? schedule.startAt),
            endAt: toDateTimeLocalInput(schedule.endTime ?? schedule.endAt),
        })),
    }
}

function buildEventPayload(draft: EventDraft) {
    const schedules = (draft.schedules?.length
        ? draft.schedules
        : [{
            name: draft.eventName || '메인 일정',
            startAt: draft.startAt,
            endAt: draft.endAt,
        }]).map((schedule, index) => ({
        name: schedule.name || `Day ${index + 1}`,
        startTime: normalizeDateTimeInput(schedule.startAt) ?? draft.startAt,
        endTime: normalizeDateTimeInput(schedule.endAt) ?? draft.endAt,
    }))

    if (schedules.length === 1) {
        schedules[0] = {
            ...schedules[0],
            startTime: normalizeDateTimeInput(draft.startAt) ?? schedules[0].startTime,
            endTime: normalizeDateTimeInput(draft.endAt) ?? schedules[0].endTime,
        }
    }

    return {
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
        schedules,
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

function toDateTimeLocalInput(value?: string | null) {
    if (!value) return ''
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return ''
    const pad = (n: number) => String(n).padStart(2, '0')
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`
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
                        error,
                        required = false,
                    }: {
    label: string
    value: string
    onChange: (value: string) => void
    placeholder?: string
    type?: string
    error?: string
    required?: boolean
}) {
    const hasError = Boolean(error?.trim())
    const inputClassName =
        type === 'datetime-local'
            ? `w-full rounded-full border bg-slate-50 px-3 py-2 text-sm text-slate-500 outline-none [color-scheme:light] ${
                hasError ? 'border-rose-300 bg-rose-50/40' : 'border-[var(--line)]'
            }`
            : `w-full rounded-full border bg-slate-50 px-3 py-2 text-sm outline-none ${
                hasError ? 'border-rose-300 bg-rose-50/40' : 'border-[var(--line)]'
            }`

    return (
        <label className="block">
            <div className="mb-1 flex items-center gap-1 text-[11px] font-semibold text-slate-500">
                <span>{label}</span>
                {required && <span className="text-rose-500">*</span>}
            </div>
            <input
                value={value}
                type={type}
                onChange={(e) => onChange(e.target.value)}
                placeholder={placeholder}
                className={inputClassName}
            />
            {error && <div className="mt-1 text-[11px] leading-5 text-rose-600">{error}</div>}
        </label>
    )
}

function createEmptyEventFormErrors(): EventFormErrors {
    return {}
}

function validateEventDraft(draft: EventDraft): EventFormErrors {
    const errors: EventFormErrors = {}

    if (!draft.eventName.trim()) errors.eventName = '행사명은 필수입니다.'
    if (!draft.categoryId.trim()) errors.categoryId = '카테고리는 필수입니다.'
    if (!draft.startAt.trim()) errors.startAt = '시작일은 필수입니다.'
    if (!draft.endAt.trim()) errors.endAt = '종료일은 필수입니다.'
    if (!draft.place.trim()) errors.place = '장소는 필수입니다.'
    if (!draft.latitude.trim()) errors.latitude = '위도는 필수입니다.'
    if (!draft.longitude.trim()) errors.longitude = '경도는 필수입니다.'
    if (!draft.minFee.trim()) errors.minFee = '최소 금액은 필수입니다.'
    if (!draft.maxFee.trim()) errors.maxFee = '최대 금액은 필수입니다.'
    if (!draft.description.trim()) errors.description = '설명은 필수입니다.'

    if (draft.hasTicketing) {
        if (!draft.ticketingOpenAt.trim()) errors.ticketingOpenAt = '티켓팅 오픈은 필수입니다.'
        if (!draft.ticketingCloseAt.trim()) errors.ticketingCloseAt = '티켓팅 종료는 필수입니다.'
    }

    return errors
}

function mapEventFormErrors(message: string): EventFormErrors {
    const text = message.trim()
    const errors: EventFormErrors = {}

    if (/설명/.test(text)) errors.description = text
    if (/행사명|제목|이름/.test(text)) errors.eventName = text
    if (/카테고리/.test(text)) errors.categoryId = text
    if (/장소|주소/.test(text)) errors.place = text
    if (/위도/.test(text)) errors.latitude = text
    if (/경도/.test(text)) errors.longitude = text
    if (/현재 시간 이후/.test(text)) {
        errors.startAt = '이미 시작한 행사는 시작일은 그대로 두고 종료일만 수정할 수 있어요.'
    } else if (/시작/.test(text)) {
        errors.startAt = '시작일과 종료일을 다시 확인해 주세요.'
    }
    if (/종료/.test(text)) errors.endAt = '종료일은 시작일보다 뒤여야 해요.'
    if (/티켓팅.*오픈/.test(text)) errors.ticketingOpenAt = text
    if (/티켓팅.*종료/.test(text)) errors.ticketingCloseAt = text
    if (/티켓팅.*링크/.test(text)) errors.ticketingLink = text
    if (/공식.*링크/.test(text)) errors.officialLink = text
    if (/출연|아티스트|가수|performer/i.test(text)) errors.performer = text
    if (/이미지|사진/.test(text)) errors.img = text
    if (/반경/.test(text)) errors.radius = text

    return errors
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
                              submitLabel,
                              fieldErrors,
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
    submitLabel: string
    fieldErrors?: EventFormErrors
}) {
    const categoryValue = draft.categoryId || categoryOptions[0]?.id || ''
    const categoryLabel = categoryOptions.find((category) => category.id === categoryValue)?.name ?? '선택 안 됨'
    const imageInputRef = useRef<HTMLInputElement | null>(null)
    const hasCoordinates = Boolean(draft.latitude.trim()) && Boolean(draft.longitude.trim())
    const hasPlace = Boolean(draft.place.trim())
    const canSubmit = hasPlace && hasCoordinates
    const scheduleCount = draft.schedules?.length ?? 0
    const mergedErrors = fieldErrors ?? {}

    return (
        <div className="w-full max-w-full overflow-x-hidden rounded-[22px] border border-[var(--line)] bg-white p-4">
            <div className="flex items-start justify-between gap-3">
                <div>
                    <div className="text-sm font-semibold text-slate-950">{title}</div>
                    <div className="mt-1 text-xs leading-5 text-slate-500">{subtitle}</div>
                    <div className="mt-1 text-[11px] leading-5 text-slate-400">* 표시가 있는 항목은 필수예요.</div>
                </div>
                <button type="button" onClick={onClose}
                        className="rounded-full border border-[var(--line)] bg-white px-3 py-1.5 text-xs font-semibold text-slate-600">
                    닫기
                </button>
            </div>

            <div className="mt-4 grid gap-3 grid-cols-1 md:grid-cols-2">
                <AdminInput
                    label="행사명"
                    value={draft.eventName}
                    onChange={(value) => setDraft({eventName: value})}
                    error={mergedErrors.eventName}
                    required
                />
                <label className="block">
                    <div className="mb-1 flex items-center gap-1 text-[11px] font-semibold text-slate-500">
                        <span>카테고리</span>
                        <span className="text-rose-500">*</span>
                    </div>
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
                    {mergedErrors.categoryId && (
                        <div className="mt-1 text-[11px] leading-5 text-rose-600">{mergedErrors.categoryId}</div>
                    )}
                </label>
                <AdminInput
                    label="시작일"
                    value={draft.startAt}
                    onChange={(value) => setDraft({startAt: value})}
                    type="datetime-local"
                    error={mergedErrors.startAt}
                    required
                />
                <AdminInput
                    label="종료일"
                    value={draft.endAt}
                    onChange={(value) => setDraft({endAt: value})}
                    type="datetime-local"
                    error={mergedErrors.endAt}
                    required
                />
                <label className="md:col-span-2 block">
                    <div
                        className="mb-1 flex items-center justify-between gap-2 text-[11px] font-semibold text-slate-500">
                        <span>장소</span>
                        <span className="text-rose-500">*</span>
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
                    {mergedErrors.place && (
                        <div className="mt-1 text-[11px] leading-5 text-rose-600">{mergedErrors.place}</div>
                    )}
                </label>
                <div className="md:col-span-2 grid gap-3 min-[900px]:grid-cols-4">
                    <AdminInput
                        label="지역"
                        value={draft.region}
                        onChange={(value) => setDraft({region: value})}
                        placeholder="자동 입력"
                        error={mergedErrors.region}
                    />
                    <AdminInput
                        label="위도"
                        value={draft.latitude}
                        onChange={(value) => setDraft({latitude: value})}
                        placeholder="37.5665"
                        type="number"
                        error={mergedErrors.latitude}
                        required
                    />
                    <AdminInput
                        label="경도"
                        value={draft.longitude}
                        onChange={(value) => setDraft({longitude: value})}
                        placeholder="126.9780"
                        type="number"
                        error={mergedErrors.longitude}
                        required
                    />
                    <AdminInput
                        label="반경"
                        value={draft.radius}
                        onChange={(value) => setDraft({radius: value})}
                        placeholder="500"
                        type="number"
                        error={mergedErrors.radius}
                    />
                </div>
                <AdminInput
                    label="최소 금액"
                    value={draft.minFee}
                    onChange={(value) => setDraft({minFee: value})}
                    placeholder="0"
                    type="number"
                    error={mergedErrors.minFee}
                    required
                />
                <AdminInput
                    label="최대 금액"
                    value={draft.maxFee}
                    onChange={(value) => setDraft({maxFee: value})}
                    placeholder="150000"
                    type="number"
                    error={mergedErrors.maxFee}
                    required
                />
                <AdminInput
                    label="공식 링크"
                    value={draft.officialLink}
                    onChange={(value) => setDraft({officialLink: value})}
                    error={mergedErrors.officialLink}
                />
                <AdminInput
                    label="출연"
                    value={draft.performer}
                    onChange={(value) => setDraft({performer: value})}
                    error={mergedErrors.performer}
                />
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
                    <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
                            <AdminInput label="티켓팅 오픈" value={draft.ticketingOpenAt}
                                        onChange={(value) => setDraft({ticketingOpenAt: value})} type="datetime-local"
                                        error={mergedErrors.ticketingOpenAt} required={draft.hasTicketing}/>
                            <AdminInput label="티켓팅 종료" value={draft.ticketingCloseAt}
                                        onChange={(value) => setDraft({ticketingCloseAt: value})}
                                        type="datetime-local"
                                        error={mergedErrors.ticketingCloseAt} required={draft.hasTicketing}/>
                            <div className="md:col-span-2">
                                <AdminInput label="티켓팅 링크" value={draft.ticketingLink}
                                            onChange={(value) => setDraft({ticketingLink: value})}
                                            error={mergedErrors.ticketingLink}/>
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
                            className={`min-w-0 flex-1 rounded-full border bg-slate-50 px-3 py-2 text-sm outline-none ${
                                mergedErrors.img ? 'border-rose-300 bg-rose-50/40' : 'border-[var(--line)]'
                            }`}
                        />
                    </div>
                    <div className="mt-1 text-[11px] leading-5 text-slate-400">
                        {draft.imgFileName ? `첨부 파일: ${draft.imgFileName}` : 'URL 또는 파일 첨부 둘 다 사용할 수 있어요.'}
                    </div>
                    {mergedErrors.img && <div className="mt-1 text-[11px] leading-5 text-rose-600">{mergedErrors.img}</div>}
                </label>

                <div className="flex flex-col gap-3 min-[700px]:col-span-2 min-[700px]:flex-row min-[700px]:items-center min-[700px]:justify-between">
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

                    <div className="min-[700px]:col-span-2 space-y-2">
                    {(draft.schedules ?? []).map((schedule, index) => (
                        <div key={index}
                             className="grid grid-cols-1 gap-2 rounded-[18px] border border-[var(--line)] bg-slate-50 p-3 min-[700px]:grid-cols-2 xl:grid-cols-[1.2fr_1fr_1fr_auto] xl:items-end">
                            <div className="min-[700px]:col-span-2 xl:col-span-1">
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
                                    error={mergedErrors.eventName}
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
                                error={mergedErrors.startAt}
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
                                error={mergedErrors.endAt}
                            />
                            <button
                                type="button"
                                onClick={() =>
                                    setDraft({
                                        schedules: (draft.schedules ?? []).filter((_, itemIndex) => itemIndex !== index),
                                    })
                                }
                                className="w-full rounded-full border border-rose-200 bg-rose-50 px-3 py-2 text-xs font-semibold text-rose-700 min-[700px]:col-span-2 xl:col-span-1"
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
                <label className="min-[700px]:col-span-2 block">
                    <div className="mb-1 flex items-center gap-1 text-[11px] font-semibold text-slate-500">
                        <span>설명</span>
                        <span className="text-rose-500">*</span>
                    </div>
                    <textarea
                        value={draft.description}
                        onChange={(e) => setDraft({description: e.target.value})}
                        className={`min-h-24 w-full rounded-[18px] border bg-slate-50 p-3 text-sm outline-none ${
                            mergedErrors.description ? 'border-rose-300 bg-rose-50/40' : 'border-[var(--line)]'
                        }`}
                    />
                    {mergedErrors.description && (
                        <div className="mt-1 text-[11px] leading-5 text-rose-600">{mergedErrors.description}</div>
                    )}
                </label>
            </div>

            <div className="mt-4 rounded-[20px] border border-slate-200 bg-slate-50 p-4">
                <div className="text-[11px] font-semibold text-slate-500">등록 미리보기</div>
                <div className="mt-3 grid gap-3 text-sm text-slate-700 md:grid-cols-2">
                    <PreviewField label="행사명" value={draft.eventName || '미입력'} />
                    <PreviewField label="카테고리" value={categoryLabel} />
                    <PreviewField label="기간" value={draft.startAt && draft.endAt ? `${draft.startAt} ~ ${draft.endAt}` : '미입력'} />
                    <PreviewField label="장소" value={draft.place || '미입력'} />
                    <PreviewField label="티켓팅" value={draft.hasTicketing ? '있음' : '없음'} />
                    <PreviewField label="일정 개수" value={`${scheduleCount}개`} />
                </div>
            </div>

            <div className="mt-4 flex items-center justify-end gap-2">
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
                    {loading ? '처리 중...' : submitLabel}
                </button>
            </div>
        </div>
    )
}

function PreviewField({label, value}: { label: string; value: string }) {
    return (
        <div className="rounded-[16px] border border-white/80 bg-white px-3 py-2">
            <div className="text-[10px] font-semibold text-slate-500">{label}</div>
            <div className="mt-1 text-sm font-medium text-slate-800">{value}</div>
        </div>
    )
}

function DetailField({label, value}: { label: string; value: string }) {
    return (
        <div className="rounded-[18px] border border-[var(--line)] bg-slate-50 px-4 py-3">
            <div className="text-[11px] font-semibold text-slate-500">{label}</div>
            <div className="mt-1 break-words text-sm font-medium text-slate-900">{value}</div>
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
    if (value === 'manage' || value === 'reports' || value === 'chat' || value === 'users' || value === 'blacklist' || value === 'notifications') return value
    return 'manage'
}

function normalizeEventCategoryKey(name: string) {
    if (name === '\uCF58\uC11C\uD2B8') return 'concert'
    if (name === '\uCD95\uC81C') return 'festival'
    if (name === '\uD32C\uBBF8\uD305') return 'fanmeeting'
    if (name === '\uD31D\uC5C5\uC2A4\uD1A0\uC5B4') return 'popup'
    return name.toLowerCase()
}

function labelUserRole(role: string) {
    const normalized = String(role ?? '').trim()
    const map: Record<string, string> = {
        USER: '일반 사용자',
        ADMIN: '관리자',
        CONCERT_MANAGER: '콘서트 매니저',
        FESTIVAL_MANAGER: '축제 매니저',
        FANMEETING_MANAGER: '팬미팅 매니저',
        POPUP_MANAGER: '팝업 매니저',
        COMMUNITY_MANAGER: '커뮤니티 매니저',
    }
    return map[normalized] ?? (normalized || '알 수 없음')
}

function labelUserStatus(status: AdminUserStatus | string) {
    const normalized = String(status ?? '').trim().toUpperCase()
    const map: Record<string, string> = {
        ACTIVE: 'ACTIVE',
        BLOCKED: 'BLOCKED',
    }
    return map[normalized] ?? (normalized || '알 수 없음')
}

function labelBlacklistStatus(status: BlacklistStatus | string) {
    const normalized = String(status ?? '').trim().toUpperCase()
    const map: Record<string, string> = {
        ACTIVE: '차단 중',
        INACTIVE: '차단 해제됨',
    }
    return map[normalized] ?? (normalized || '알 수 없음')
}

function labelReportStatus(status: string) {
    const normalized = String(status ?? '').trim().toUpperCase()
    const map: Record<string, string> = {
        PENDING: '접수됨',
        AUTO_BLINDED: '자동 블라인드',
        RESOLVED: '제재 확정',
        REJECTED: '반려',
    }
    return map[normalized] ?? (normalized || '알 수 없음')
}

function labelReportTargetType(targetType: string) {
    const normalized = String(targetType ?? '').trim().toUpperCase()
    const map: Record<string, string> = {
        POST: '게시글',
        COMMENT: '댓글',
        CHAT: '채팅',
    }
    return map[normalized] ?? (normalized || '알 수 없음')
}

function labelReporterType(reporterType: string) {
    const normalized = String(reporterType ?? '').trim().toUpperCase()
    if (normalized === 'SYSTEM_AI') return 'AI 신고'
    return '사용자 신고'
}

function labelReportFilter(value: string) {
    const normalized = String(value ?? '').trim().toUpperCase()
    if (normalized === 'ALL') return '전체'
    return labelReportStatus(normalized)
}

function extractErrorMessage(error: any, fallback: string) {
    return error?.response?.data?.message
        ?? error?.response?.data?.error
        ?? error?.message
        ?? fallback
}
