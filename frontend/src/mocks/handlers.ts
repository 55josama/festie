import { http, HttpResponse, delay } from 'msw'
import {
  mockEvents,
  mockPosts,
  mockMessages,
  mockEventCategories,
  mockCategories,
  mockChatRooms,
  mockComments,
  mockCalendars,
  mockEventRequests,
  mockOperationRequests,
  mockBlacklists,
  mockFavorites,
  mockReports,
  mockNotifications,
  mockNotices,
} from './data'

const wrap = (data: any) => ({ status: 'success', data })

type MockProfile = {
  userId: string
  nickname: string
  email: string
  name: string
  phoneNumber: string
  role: 'ADMIN' | 'MANAGER' | 'USER'
}

type MockAdminUser = {
  userId: string
  email: string
  nickname: string
  name: string
  phoneNumber: string
  role: 'USER' | 'ADMIN' | 'CONCERT_MANAGER' | 'FESTIVAL_MANAGER' | 'FANMEETING_MANAGER' | 'POPUP_MANAGER' | 'COMMUNITY_MANAGER'
  status: 'ACTIVE' | 'BLOCKED'
  createdAt: string
  updatedAt: string
}

const mockProfiles = new Map<string, MockProfile>([
  ['mock-admin-token', {
    userId: 'admin-user',
    nickname: '관리자',
    email: 'admin@festie.kr',
    name: '관리자',
    phoneNumber: '010-9999-9999',
    role: 'ADMIN',
  }],
  ['mock-manager-token', {
    userId: 'manager-user',
    nickname: '매니저',
    email: 'manager@festie.kr',
    name: '매니저',
    phoneNumber: '010-8888-8888',
    role: 'MANAGER',
  }],
  ['mock-token', {
    userId: 'me',
    nickname: '테스트유저',
    email: 'test@festie.kr',
    name: '테스트',
    phoneNumber: '010-1234-5678',
    role: 'USER',
  }],
])

const mockAdminUsers: MockAdminUser[] = [
  {
    userId: 'admin-user',
    email: 'admin@festie.com',
    nickname: '어드민',
    name: '관리자',
    phoneNumber: '010-1111-1111',
    role: 'ADMIN',
    status: 'ACTIVE',
    createdAt: '2026-05-01T09:00:00.000Z',
    updatedAt: '2026-05-15T09:00:00.000Z',
  },
  {
    userId: 'manager-user',
    email: 'manager@festie.com',
    nickname: '매니저',
    name: '매니저',
    phoneNumber: '010-2222-2222',
    role: 'CONCERT_MANAGER',
    status: 'ACTIVE',
    createdAt: '2026-05-02T09:00:00.000Z',
    updatedAt: '2026-05-15T09:10:00.000Z',
  },
  {
    userId: 'u-1001',
    email: 'subin@festie.com',
    nickname: '호잇호잇',
    name: '수빈',
    phoneNumber: '010-3333-3333',
    role: 'USER',
    status: 'ACTIVE',
    createdAt: '2026-05-03T09:00:00.000Z',
    updatedAt: '2026-05-15T09:20:00.000Z',
  },
  {
    userId: 'u-1002',
    email: 'popup@example.com',
    nickname: '팝업덕후',
    name: '팝업덕후',
    phoneNumber: '010-4444-4444',
    role: 'POPUP_MANAGER',
    status: 'BLOCKED',
    createdAt: '2026-05-04T09:00:00.000Z',
    updatedAt: '2026-05-15T09:30:00.000Z',
  },
]

function normalizeCategoryKey(name: string) {
  if (name === '\uCF58\uC11C\uD2B8') return 'concert'
  if (name === '\uCD95\uC81C') return 'festival'
  if (name === '\uD32C\uBBF8\uD305') return 'fanmeeting'
  if (name === '\uD31D\uC5C5\uC2A4\uD1A0\uC5B4') return 'popup'
  return String(name).toLowerCase()
}

function syncMockUserStatus(userId: string, status: 'ACTIVE' | 'BLOCKED') {
  const user = mockAdminUsers.find((item) => item.userId === userId)
  if (user) {
    user.status = status
    user.updatedAt = new Date().toISOString()
  }
}

function buildChatbotReply(question: string) {
  const q = question.trim().toLowerCase()
  const matchedEvent = mockEvents.find((event) => {
    const name = String(event.name ?? '').toLowerCase()
    const category = String(event.categoryName ?? '').toLowerCase()
    const place = String(event.place ?? '').toLowerCase()
    return q.includes(name) || q.includes(category) || q.includes(place)
  })

  if (q.includes('이용') || q.includes('사용') || q.includes('어떻게') || q.includes('방법')) {
    return 'Festie에서는 행사 목록에서 원하는 행사를 찾고, 행사 상세에서 찜하기와 채팅방을 이용할 수 있어요. 커뮤니티에서는 후기와 꿀팁을, MY에서는 내 일정과 관심 목록을 확인할 수 있어요.'
  }

  if (q.includes('이번주') || q.includes('이번 주') || q.includes('이주') || q.includes('이번달')) {
    const now = new Date()
    const in7Days = new Date(now.getTime() + 1000 * 60 * 60 * 24 * 7)
    const upcoming = mockEvents
      .filter((event) => new Date(event.startAt) >= now && new Date(event.startAt) <= in7Days)
      .sort((a, b) => new Date(a.startAt).getTime() - new Date(b.startAt).getTime())
      .slice(0, 3)
    if (upcoming.length > 0) {
      return `이번 주 추천 행사: ${upcoming.map((event) => event.name).join(' · ')}`
    }
    return '이번 주에 바로 보이는 행사는 많지 않아요. 지역이나 카테고리를 알려주시면 더 좁혀서 찾아드릴게요.'
  }

  if (q.includes('인기') || q.includes('찜') || q.includes('많은 행사') || q.includes('인기많은')) {
    return '찜 수 기반 인기 순위는 아직 직접 연결하지 않았어요. 대신 지금 주목할 만한 행사나 비슷한 행사를 추천해드릴게요.'
  }

  if (q.includes('요약') || q.includes('정리')) {
    if (matchedEvent) {
      return `${matchedEvent.name}는 ${matchedEvent.categoryName} 행사예요. ${matchedEvent.place}에서 ${matchedEvent.startAt?.slice(0, 10) ?? '미정'}부터 ${matchedEvent.endAt?.slice(0, 10) ?? '미정'}까지 진행돼요. ${matchedEvent.hasTicketing ? '티켓팅이 있어요.' : '티켓팅 없이 관람할 수 있어요.'}`
    }
    return '원하는 행사 이름을 함께 알려주시면 행사명, 장소, 일정, 티켓팅 여부를 중심으로 짧게 요약해드릴게요.'
  }

  if (q.includes('비슷') || q.includes('추천') || q.includes('비교')) {
    const category = matchedEvent?.categoryName
    const candidates = mockEvents.filter((event) => !matchedEvent || event.id !== matchedEvent.id)
      .filter((event) => !category || event.categoryName === category)
      .slice(0, 3)
    if (candidates.length > 0) {
      return `비슷한 행사 추천: ${candidates.map((event) => `${event.name} · ${event.place}`).join(' / ')}`
    }
    return '비슷한 행사를 찾지 못했어요. 행사 이름이나 카테고리를 조금 더 자세히 알려주세요.'
  }

  if (q.includes('지역') || q.includes('서울') || q.includes('부산') || q.includes('대구') || q.includes('인천') || q.includes('광주') || q.includes('대전') || q.includes('울산')) {
    const region = ['서울', '부산', '대구', '인천', '광주', '대전', '울산'].find((item) => q.includes(item))
    const regionEvents = mockEvents.filter((event) => String(event.region ?? '').includes(region ?? ''))
    if (regionEvents.length > 0) {
      return `${region} 지역 행사: ${regionEvents.slice(0, 3).map((event) => event.name).join(' · ')}`
    }
    return '어느 지역을 원하세요? 서울, 부산, 대구, 인천처럼 지역을 알려주시면 찾아드릴게요.'
  }

  if (matchedEvent) {
    return `${matchedEvent.name}는 ${matchedEvent.categoryName} 행사이고, ${matchedEvent.place}에서 열려요. ${matchedEvent.hasTicketing ? '티켓팅이 있는 행사예요.' : '티켓팅 없이 진행되는 행사예요.'}`
  }

  return 'Festie 이용 방법, 이번 주 행사, 비슷한 행사 추천, 지역 행사 정보까지 도와드릴 수 있어요. 궁금한 행사가 있으면 이름이나 지역을 알려주세요.'
}

function wrapNotificationPage(page: number, size: number) {
  const start = page * size
  const content = mockNotifications.slice(start, start + size)
  return {
    content,
    page,
    size,
    totalElements: mockNotifications.length,
    totalPages: Math.max(1, Math.ceil(mockNotifications.length / size)),
  }
}

export const handlers = [
  http.get('/event-service/v1/events', async ({ request }) => {
    await delay(250)
    const url = new URL(request.url)
    const hasTicketing = url.searchParams.get('hasTicketing')
    let events = [...mockEvents]
    if (hasTicketing === 'true') {
      events = events.filter((event) => event.hasTicketing)
    } else if (hasTicketing === 'false') {
      events = events.filter((event) => !event.hasTicketing)
    }
    return HttpResponse.json(wrap({ content: events, totalElements: events.length, totalPages: 1, size: 10, number: 0 }))
  }),

  http.get('/event-service/v1/events/:eventId', async ({ params }) => {
    await delay(150)
    const event = mockEvents.find(e => e.id === params.eventId)
    if (!event) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    return HttpResponse.json(wrap(event))
  }),

  http.get('/notification-service/v1/notifications', async ({ request }) => {
    await delay(120)
    const url = new URL(request.url)
    const page = Number(url.searchParams.get('page') ?? 0)
    const size = Number(url.searchParams.get('size') ?? 10)
    return HttpResponse.json(wrap(wrapNotificationPage(page, size)))
  }),

  http.patch('/notification-service/v1/notifications', async () => {
    await delay(100)
    const unread = mockNotifications.filter((notification) => !notification.readAt)
    const now = new Date().toISOString()
    mockNotifications.forEach((notification) => {
      if (!notification.readAt) {
        notification.readAt = now
      }
    })
    return HttpResponse.json(wrap(unread.map((notification) => ({ ...notification, readAt: now }))))
  }),

  http.delete('/notification-service/v1/notifications/:notificationId', async ({ params }) => {
    await delay(80)
    const index = mockNotifications.findIndex((item) => item.id === params.notificationId)
    if (index === -1) {
      return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    }
    mockNotifications.splice(index, 1)
    return HttpResponse.json(wrap({}))
  }),

  http.post('/event-service/v1/events', async ({ request }) => {
    await delay(220)
    const body = await request.json() as any
    const categoryId = body.categoryId ?? ({ concert: 'c1', festival: 'c2', fanmeeting: 'c3', popup: 'c4' } as Record<string, string>)[normalizeCategoryKey(body.categoryName ?? body.category ?? '\uCF58\uC11C\uD2B8')] ?? 'c1'
    const categoryName = mockEventCategories.find((item) => item.id === categoryId)?.name ?? body.categoryName ?? body.category ?? '기타'
    const nextId = crypto.randomUUID()
    const nextChatRoomId = `room-${crypto.randomUUID()}`
    const created = {
      id: nextId,
      name: body.name ?? body.eventName ?? '새 행사',
      categoryId,
      categoryName,
      startAt: body.startAt ?? new Date().toISOString(),
      endAt: body.endAt ?? body.startAt ?? new Date().toISOString(),
      place: body.place ?? '미정',
      region: body.region ?? '서울',
      latitude: body.latitude ?? null,
      longitude: body.longitude ?? null,
      radius: body.radius ?? null,
      minFee: body.minFee ?? null,
      maxFee: body.maxFee ?? null,
      hasTicketing: Boolean(body.hasTicketing ?? body.ticketingLink),
      ticketingOpenAt: body.ticketingOpenAt ?? null,
      ticketingCloseAt: body.ticketingCloseAt ?? null,
      ticketingLink: body.ticketingLink ?? null,
      officialLink: body.officialLink ?? null,
      description: body.description ?? null,
      performer: body.performer ?? null,
      img: body.img ?? null,
      status: body.status ?? 'SCHEDULED',
      schedules: body.schedules ?? [],
      chatRoomId: nextChatRoomId,
    }
    mockEvents.push(created as any)
    mockChatRooms.push({
      chatRoomId: nextChatRoomId,
      eventId: nextId,
      eventName: created.name,
      category: categoryName.toUpperCase(),
      status: 'SCHEDULED',
      scheduledOpenAt: body.schedules?.[0]?.startTime ?? created.startAt,
      scheduledCloseAt: body.schedules?.[0]?.endTime ?? created.endAt,
      openedAt: null,
      closedAt: null,
      currentViewerCount: 0,
    } as any)
    if (body.requestId) {
      const request = mockEventRequests.find((item) => item.id === body.requestId)
      if (request) {
        request.createdEventId = nextId
        request.status = 'APPROVED'
      }
    }
    return HttpResponse.json(wrap(created), { status: 201 })
  }),

  http.patch('/event-service/v1/events/:eventId', async ({ params, request }) => {
    await delay(200)
    const body = await request.json() as any
    const event = mockEvents.find((item) => item.id === params.eventId) as any
    if (!event) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    const categoryId = body.categoryId ?? event.categoryId
    const categoryName = mockEventCategories.find((item) => item.id === categoryId)?.name ?? event.categoryName
    event.name = body.name ?? event.name
    event.categoryId = categoryId
    event.categoryName = categoryName
    event.startAt = body.startAt ?? event.startAt
    event.endAt = body.endAt ?? event.endAt
    event.place = body.place ?? event.place
    event.region = body.region ?? event.region
    event.latitude = body.latitude ?? event.latitude
    event.longitude = body.longitude ?? event.longitude
    event.radius = body.radius ?? event.radius
    event.minFee = body.minFee ?? event.minFee
    event.maxFee = body.maxFee ?? event.maxFee
    event.hasTicketing = body.hasTicketing ?? event.hasTicketing
    event.ticketingOpenAt = body.ticketingOpenAt ?? event.ticketingOpenAt
    event.ticketingCloseAt = body.ticketingCloseAt ?? event.ticketingCloseAt
    event.ticketingLink = body.ticketingLink ?? event.ticketingLink
    event.officialLink = body.officialLink ?? event.officialLink
    event.description = body.description ?? event.description
    event.performer = body.performer ?? event.performer
    event.img = body.img ?? event.img
    event.schedules = body.schedules ?? event.schedules
    const room = mockChatRooms.find((item) => item.eventId === event.id) as any
    if (room) {
      room.eventName = event.name
      room.category = categoryName.toUpperCase()
      room.scheduledOpenAt = body.schedules?.[0]?.startTime ?? room.scheduledOpenAt
      room.scheduledCloseAt = body.schedules?.[0]?.endTime ?? room.scheduledCloseAt
    }
    return HttpResponse.json(wrap(event))
  }),

  http.post('/event-service/v1/event-requests', async ({ request }) => {
    await delay(180)
    const body = await request.json() as any
    const requesterId = request.headers.get('x-user-id') ?? 'me'
    const title = String(body.title ?? '').trim()
    const categoryId = String(body.categoryId ?? '').trim()
    const link = String(body.link ?? '').trim()
    const description = String(body.description ?? '').trim()
    if (!title || !categoryId || !link) {
      return HttpResponse.json({ status: 'error', message: 'Invalid request' }, { status: 400 })
    }
    const category = mockEventCategories.find((item) => item.id === categoryId)
    if (!category) {
      return HttpResponse.json({ status: 'error', message: '카테고리를 찾을 수 없습니다.' }, { status: 404 })
    }
    const created = {
      id: `req-${crypto.randomUUID()}`,
      requesterId,
      requesterNickname: requesterId === 'admin-user' ? '관리자' : requesterId === 'manager-user' ? '매니저' : '테스트유저',
      createdAt: new Date().toISOString(),
      eventName: title,
      categoryId,
      category: category.name,
      link,
      description,
      rejectReason: null,
      status: 'PENDING',
      createdEventId: null,
    }
    mockEventRequests.unshift(created as any)
    return HttpResponse.json(wrap(created), { status: 201 })
  }),

  http.post('/operation-service/v1/operation-requests', async ({ request }) => {
    await delay(180)
    const body = await request.json() as any
    const requesterId = request.headers.get('x-user-id') ?? 'me'
    const title = String(body.title ?? '').trim()
    const content = String(body.content ?? '').trim()
    if (!title || !content) {
      return HttpResponse.json({ status: 'error', message: 'Invalid request' }, { status: 400 })
    }
    const created = {
      id: `op-${crypto.randomUUID()}`,
      requesterId,
      title,
      content,
      status: 'PENDING',
      adminMemo: null,
    }
    mockOperationRequests.unshift(created as any)
    return HttpResponse.json(wrap(created), { status: 201 })
  }),

  http.get('/community-service/v1/posts', async ({ request }) => {
    await delay(250)
    const url = new URL(request.url)
    const categoryId = url.searchParams.get('categoryId')
    const sort = url.searchParams.get('sort')
    let posts = [...mockPosts]
    if (categoryId) posts = posts.filter((item) => item.categoryId === categoryId)
    if (sort === 'likeCount,desc') posts = posts.sort((a, b) => b.likeCount - a.likeCount)
    else posts = posts.sort((a, b) => String(b.createdAt).localeCompare(String(a.createdAt)))
    return HttpResponse.json(wrap({ content: posts, totalElements: posts.length, totalPages: 1, size: 10, number: 0 }))
  }),

  http.get('/community-service/v1/posts/:postId', async ({ params }) => {
    await delay(120)
    const post = mockPosts.find((item) => item.id === params.postId)
    if (!post) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    return HttpResponse.json(wrap(post))
  }),

  http.post('/community-service/v1/posts/:postId/likes', async ({ params }) => {
    await delay(90)
    const post = mockPosts.find((item) => item.id === params.postId)
    if (!post) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    post.likeCount = (post.likeCount ?? 0) + 1
    return HttpResponse.json(wrap({}))
  }),

  http.delete('/community-service/v1/posts/:postId/likes', async ({ params }) => {
    await delay(90)
    const post = mockPosts.find((item) => item.id === params.postId)
    if (!post) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    post.likeCount = Math.max(0, (post.likeCount ?? 0) - 1)
    return HttpResponse.json(wrap({}))
  }),

  http.get('/community-service/v1/posts/:postId/comments', async ({ params }) => {
    await delay(150)
    const comments = mockComments.filter((item) => item.postId === params.postId)
    return HttpResponse.json(wrap({ content: comments, totalElements: comments.length, totalPages: 1, size: 20, number: 0 }))
  }),

  http.post('/community-service/v1/posts', async ({ request }) => {
    await delay(150)
    const body = await request.json() as any
    const category = mockCategories.find((item) => item.id === body.categoryId)
    const created = {
      id: `post-${Date.now()}-${Math.floor(Math.random() * 1000)}`,
      userId: 'me',
      categoryId: body.categoryId,
      categoryName: category?.name ?? '자유',
      title: body.title,
      content: body.content,
      viewCount: 0,
      likeCount: 0,
      commentCount: 0,
      status: 'CLEAN',
      visibility: body.visibility ?? 'PUBLIC',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      authorNickname: '테스트유저',
    }
    mockPosts.unshift(created as any)
    return HttpResponse.json(wrap(created), { status: 201 })
  }),

  http.post('/operation-service/v1/reports', async ({ request }) => {
    await delay(150)
    const body = await request.json() as any
    const reporterId = request.headers.get('x-user-id') ?? 'me'
    const reporterType = request.headers.get('x-user-role') ?? 'USER'
    const created = {
      id: `report-${Date.now()}`,
      reporterId,
      reporterType,
      targetId: body.targetId,
      targetUserId: body.targetUserId ?? 'u1',
      targetType: body.targetType,
      category: body.category ?? 'OTHER',
      description: body.description ?? '신고',
      status: 'AUTO_BLINDED',
      operatorMemo: null,
    }
    mockReports.unshift(created as any)
    return HttpResponse.json(wrap(created), { status: 201 })
  }),

  http.patch('/community-service/v1/posts/:postId', async ({ params, request }) => {
    await delay(150)
    const body = await request.json() as any
    const post = mockPosts.find((item) => item.id === params.postId)
    if (!post) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    post.title = body.title ?? post.title
    post.content = body.content ?? post.content
    post.categoryId = body.categoryId ?? post.categoryId
    return HttpResponse.json(wrap(post))
  }),

  http.delete('/community-service/v1/posts/:postId', async ({ params, request }) => {
    await delay(120)
    const userId = request.headers.get('x-user-id')
    const role = request.headers.get('x-user-role')
    const index = mockPosts.findIndex((item) => item.id === params.postId)
    if (index === -1) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    const post = mockPosts[index]
    if (role !== 'ADMIN' && userId !== post.userId) {
      return HttpResponse.json({ status: 'error', message: 'Forbidden' }, { status: 403 })
    }
    mockPosts.splice(index, 1)
    return HttpResponse.json(wrap({}))
  }),

  http.delete('/community-service/v1/comments/:commentId', async ({ params, request }) => {
    await delay(120)
    const userId = request.headers.get('x-user-id')
    const role = request.headers.get('x-user-role')
    const index = mockComments.findIndex((item) => item.id === params.commentId)
    if (index === -1) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    const comment = mockComments[index]
    if (role !== 'ADMIN' && userId !== comment.userId) {
      return HttpResponse.json({ status: 'error', message: 'Forbidden' }, { status: 403 })
    }
    mockComments.splice(index, 1)
    return HttpResponse.json(wrap({}))
  }),

  http.get('/community-service/v1/community-categories', async () => {
    await delay(100)
    return HttpResponse.json(wrap(mockCategories))
  }),

  http.get('/event-service/v1/event-categories', async () => {
    await delay(100)
    return HttpResponse.json(wrap(mockEventCategories))
  }),

  http.post('/event-service/v1/event-categories', async ({ request }) => {
    await delay(120)
    const body = await request.json() as any
    const created = { id: `ec-${Date.now()}`, name: body.name }
    mockEventCategories.push(created as any)
    return HttpResponse.json(wrap(created), { status: 201 })
  }),

  http.patch('/event-service/v1/event-categories/:categoryId', async ({ params, request }) => {
    await delay(120)
    const body = await request.json() as any
    const category = mockEventCategories.find((item) => item.id === params.categoryId)
    if (!category) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    category.name = body.name ?? category.name
    return HttpResponse.json(wrap(category))
  }),

  http.delete('/event-service/v1/event-categories/:categoryId', async ({ params }) => {
    await delay(120)
    const index = mockEventCategories.findIndex((item) => item.id === params.categoryId)
    if (index === -1) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    mockEventCategories.splice(index, 1)
    return HttpResponse.json(wrap({}))
  }),

  http.post('/community-service/v1/community-categories', async ({ request }) => {
    await delay(120)
    const body = await request.json() as any
    const created = { id: `cat-${Date.now()}`, name: body.name }
    mockCategories.push(created as any)
    return HttpResponse.json(wrap(created), { status: 201 })
  }),

  http.patch('/community-service/v1/community-categories/:categoryId', async ({ params, request }) => {
    await delay(120)
    const body = await request.json() as any
    const category = mockCategories.find((item) => item.id === params.categoryId)
    if (!category) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    category.name = body.name ?? category.name
    return HttpResponse.json(wrap(category))
  }),

  http.delete('/community-service/v1/community-categories/:categoryId', async ({ params }) => {
    await delay(120)
    const index = mockCategories.findIndex((item) => item.id === params.categoryId)
    if (index === -1) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    mockCategories.splice(index, 1)
    return HttpResponse.json(wrap({}))
  }),

  http.delete('/event-service/v1/events/:eventId', async ({ params }) => {
    await delay(180)
    const index = mockEvents.findIndex((item) => item.id === params.eventId)
    if (index === -1) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    const [removed] = mockEvents.splice(index, 1)
    const roomIndex = mockChatRooms.findIndex((item) => item.eventId === removed.id)
    if (roomIndex !== -1) {
      const [removedRoom] = mockChatRooms.splice(roomIndex, 1)
      for (let i = mockMessages.length - 1; i >= 0; i -= 1) {
        if (mockMessages[i].chatRoomId === removedRoom.chatRoomId) {
          mockMessages.splice(i, 1)
        }
      }
    }
    return HttpResponse.json(wrap({}))
  }),

  http.get('/event-service/v1/event-requests', async ({ request }) => {
    await delay(180)
    const url = new URL(request.url)
    const status = url.searchParams.get('status')
    const filtered = status ? mockEventRequests.filter((item) => item.status === status) : mockEventRequests
    return HttpResponse.json(wrap({ content: filtered, totalElements: filtered.length, totalPages: 1, size: 10, number: 0 }))
  }),

  http.get('/event-service/v1/event-requests/me', async ({ request }) => {
    await delay(180)
    const userId = request.headers.get('x-user-id') ?? 'me'
    const filtered = mockEventRequests.filter((item) => item.requesterId === userId)
    return HttpResponse.json(wrap({ content: filtered, totalElements: filtered.length, totalPages: 1, size: 10, number: 0 }))
  }),

  http.get('/event-service/v1/event-requests/me/:requestId', async ({ params, request }) => {
    await delay(180)
    const userId = request.headers.get('x-user-id') ?? 'me'
    const eventRequest = mockEventRequests.find((item) => item.id === params.requestId && item.requesterId === userId)
    if (!eventRequest) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    return HttpResponse.json(wrap(eventRequest))
  }),

  http.post('/event-service/v1/event-requests/:requestId/approval', async ({ params }) => {
    await delay(150)
    const request = mockEventRequests.find((item) => item.id === params.requestId)
    if (!request) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    request.status = 'APPROVED'
    request.rejectReason = null
    return HttpResponse.json(wrap(request))
  }),

  http.post('/event-service/v1/event-requests/:requestId/rejections', async ({ params, request }) => {
    await delay(150)
    const body = await request.json() as any
    const eventRequest = mockEventRequests.find((item) => item.id === params.requestId)
    if (!eventRequest) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    eventRequest.status = 'REJECTED'
    eventRequest.rejectReason = body.rejectReason
    return HttpResponse.json(wrap(eventRequest))
  }),

  http.get('/operation-service/v1/operation-requests', async ({ request }) => {
    await delay(180)
    const url = new URL(request.url)
    const status = url.searchParams.get('status')
    const filtered = status ? mockOperationRequests.filter((item) => item.status === status) : mockOperationRequests
    return HttpResponse.json(wrap({ content: filtered, totalElements: filtered.length, totalPages: 1, size: 10, number: 0 }))
  }),

  http.get('/operation-service/v1/operation-requests/me', async ({ request }) => {
    await delay(180)
    const url = new URL(request.url)
    const status = url.searchParams.get('status')
    const userId = request.headers.get('x-user-id') ?? 'me'
    let filtered = mockOperationRequests.filter((item) => item.requesterId === userId)
    if (status) {
      filtered = filtered.filter((item) => item.status === status)
    }
    return HttpResponse.json(wrap({ content: filtered, totalElements: filtered.length, totalPages: 1, size: 10, number: 0 }))
  }),

  http.get('/operation-service/v1/operation-requests/me/:requestId', async ({ params, request }) => {
    await delay(180)
    const userId = request.headers.get('x-user-id') ?? 'me'
    const operationRequest = mockOperationRequests.find((item) => item.id === params.requestId && item.requesterId === userId)
    if (!operationRequest) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    return HttpResponse.json(wrap(operationRequest))
  }),

  http.get('/operation-service/v1/operation-requests/:requestId', async ({ params }) => {
    await delay(180)
    const operationRequest = mockOperationRequests.find((item) => item.id === params.requestId)
    if (!operationRequest) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    return HttpResponse.json(wrap(operationRequest))
  }),

  http.patch('/operation-service/v1/operation-requests/:requestId', async ({ params, request }) => {
    await delay(180)
    const body = await request.json() as any
    const operationRequest = mockOperationRequests.find((item) => item.id === params.requestId)
    if (!operationRequest) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    operationRequest.title = body.title ?? operationRequest.title
    operationRequest.content = body.content ?? operationRequest.content
    return HttpResponse.json(wrap(operationRequest))
  }),

  http.delete('/operation-service/v1/operation-requests/:requestId', async ({ params }) => {
    await delay(180)
    const index = mockOperationRequests.findIndex((item) => item.id === params.requestId)
    if (index === -1) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    mockOperationRequests.splice(index, 1)
    return HttpResponse.json(wrap({}))
  }),

  http.patch('/operation-service/v1/operation-requests/:requestId/status', async ({ params, request }) => {
    await delay(180)
    const body = await request.json() as any
    const operationRequest = mockOperationRequests.find((item) => item.id === params.requestId)
    if (!operationRequest) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    operationRequest.status = body.status
    operationRequest.adminMemo = body.adminMemo ?? operationRequest.adminMemo
    return HttpResponse.json(wrap(operationRequest))
  }),

  http.get('/operation-service/v1/reports', async ({ request }) => {
    await delay(180)
    const url = new URL(request.url)
    const status = url.searchParams.get('status')
    const filtered = status ? mockReports.filter((item) => item.status === status) : mockReports
    return HttpResponse.json(wrap({ content: filtered, totalElements: filtered.length, totalPages: 1, size: 10, number: 0 }))
  }),

  http.get('/operation-service/v1/notices', async ({ request }) => {
    await delay(140)
    const url = new URL(request.url)
    const page = Number(url.searchParams.get('page') ?? '0')
    const size = Number(url.searchParams.get('size') ?? '10')
    const start = page * size
    const content = mockNotices.slice(start, start + size)
    return HttpResponse.json(wrap({
      content,
      page,
      size,
      totalElements: mockNotices.length,
      totalPages: Math.max(1, Math.ceil(mockNotices.length / size)),
    }))
  }),

  http.get('/operation-service/v1/notices/:noticeId', async ({ params }) => {
    await delay(120)
    const notice = mockNotices.find((item) => item.noticeId === params.noticeId)
    if (!notice) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    return HttpResponse.json(wrap(notice))
  }),

  http.patch('/operation-service/v1/reports/:reportId/status', async ({ params, request }) => {
    await delay(180)
    const body = await request.json() as any
    const report = mockReports.find((item) => item.id === params.reportId)
    if (!report) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    report.status = body.status
    report.operatorMemo = body.operatorMemo
    return HttpResponse.json(wrap(report))
  }),

  http.get('/chat-service/v1/chat/rooms/event', async ({ request }) => {
    await delay(100)
    const url = new URL(request.url)
    const eventId = url.searchParams.get('eventId') ?? '1'
    const room = mockChatRooms.find((item) => item.eventId === eventId)
    if (!room) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    return HttpResponse.json(wrap(room))
  }),

  http.post('/chat-service/v1/chat/events/:eventId/location/verify', async ({ params, request }) => {
    await delay(120)
    const body = await request.json() as any
    if (body.currentLatitude == null || body.currentLongitude == null) {
      return HttpResponse.json({ status: 'error', message: '현재 위치 정보는 필수입니다.' }, { status: 400 })
    }
    return HttpResponse.json(wrap({
      eventId: params.eventId,
      isNearEvent: true,
    }))
  }),

  http.post('/chat-service/v1/chat/admin/rooms', async ({ request }) => {
    await delay(140)
    const body = await request.json() as any
    const eventId = String(body.eventId ?? '')
    const eventName = String(body.eventName ?? '').trim()
    const category = String(body.category ?? '').trim().toUpperCase()
    const scheduledOpenAt = body.scheduledOpenAt ?? null
    const scheduledCloseAt = body.scheduledCloseAt ?? null
    if (!eventId || !eventName || !category || !scheduledOpenAt || !scheduledCloseAt) {
      return HttpResponse.json({ status: 'error', message: 'Invalid request' }, { status: 400 })
    }
    if (mockChatRooms.some((item) => item.eventId === eventId)) {
      return HttpResponse.json({ status: 'error', message: '채팅방이 이미 존재합니다.' }, { status: 409 })
    }
    const chatRoomId = `room-${crypto.randomUUID()}`
    const room = {
      chatRoomId,
      eventId,
      eventName,
      category,
      status: 'SCHEDULED',
      scheduledOpenAt,
      scheduledCloseAt,
      openedAt: null,
      closedAt: null,
      currentViewerCount: 0,
    }
    mockChatRooms.push(room as any)
    return HttpResponse.json(wrap(room), { status: 201 })
  }),

  http.get('/chat-service/v1/chat/rooms/:chatRoomId/messages', async ({ params }) => {
    await delay(120)
    const messages = mockMessages.filter((item) => item.chatRoomId === params.chatRoomId)
    return HttpResponse.json(wrap({ messages, hasNext: false }))
  }),

  http.get('/chat-service/v1/chat/rooms/popular', async ({ request }) => {
    await delay(140)
    const url = new URL(request.url)
    const limit = Number(url.searchParams.get('limit') ?? 3)
    return HttpResponse.json(wrap(mockChatRooms.slice(0, limit)))
  }),

  http.get('/chat-service/v1/chat/admin/rooms', async () => {
    await delay(140)
    const rooms = [...mockChatRooms]
      .map((room) => ({
        ...room,
        currentViewerCount: room.currentViewerCount ?? 0,
      }))
      .sort((a, b) => {
        const left = a.scheduledOpenAt ? new Date(a.scheduledOpenAt).getTime() : 0
        const right = b.scheduledOpenAt ? new Date(b.scheduledOpenAt).getTime() : 0
        return right - left
      })
    return HttpResponse.json(wrap({
      content: rooms,
      totalElements: rooms.length,
      totalPages: 1,
      size: rooms.length,
      page: 0,
    }))
  }),

  http.patch('/chat-service/v1/chat/admin/rooms/:chatRoomId/status', async ({ params, request }) => {
    await delay(160)
    const body = await request.json() as any
    const room = mockChatRooms.find((item) => item.chatRoomId === params.chatRoomId)
    if (!room) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    if (body.action === 'FORCE_OPEN') {
      room.status = 'OPEN'
      room.openedAt = new Date().toISOString()
    }
    if (body.action === 'FORCE_CLOSE') {
      room.status = 'CLOSED'
      room.closedAt = new Date().toISOString()
    }
    return HttpResponse.json(wrap(room))
  }),

  http.get('/chat-service/v1/chat/admin/messages', async ({ request }) => {
    await delay(140)
    const url = new URL(request.url)
    const page = Number(url.searchParams.get('page') ?? 0)
    const size = Number(url.searchParams.get('size') ?? 20)
    const status = url.searchParams.get('status')
    const category = url.searchParams.get('category')
    const roomCategoryById = new Map(mockChatRooms.map((room) => [room.chatRoomId, room.category]))
    let messages = [...mockMessages]
    if (status) {
      messages = messages.filter((item) => item.status === status)
    }
    if (category) {
      messages = messages.filter((item) => roomCategoryById.get(item.chatRoomId) === category)
    }
    const totalElements = messages.length
    const totalPages = Math.max(1, Math.ceil(totalElements / Math.max(size, 1)))
    const start = Math.max(page, 0) * Math.max(size, 1)
    const content = messages.slice(start, start + Math.max(size, 1))
    return HttpResponse.json(wrap({ content, totalElements, totalPages, size, number: page }))
  }),

  http.patch('/chat-service/v1/chat/admin/messages/:messageId/status', async ({ params, request }) => {
    await delay(140)
    const body = await request.json() as any
    const message = mockMessages.find((item) => item.messageId === params.messageId)
    if (!message) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    message.status = body.status
    return HttpResponse.json(wrap(message))
  }),

  http.post('/chat-service/v1/chat/rooms/:chatRoomId/messages', async ({ request, params }) => {
    await delay(120)
    const body = await request.json() as any
    const message = {
      messageId: `m-${Date.now()}`,
      chatRoomId: params.chatRoomId,
      userId: 'me',
      writerNickname: '테스트유저',
      messageType: 'USER',
      content: body.content,
      status: 'ACTIVE',
      createdAt: new Date().toISOString(),
    }
    mockMessages.push(message as any)
    return HttpResponse.json(wrap(message), { status: 201 })
  }),

  http.delete('/chat-service/v1/chat/messages/:messageId', async ({ params, request }) => {
    await delay(120)
    const userId = request.headers.get('x-user-id') ?? 'me'
    const role = request.headers.get('x-user-role')
    const index = mockMessages.findIndex((item) => item.messageId === params.messageId)
    if (index === -1) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    const message = mockMessages[index]
    if (role !== 'ADMIN' && message.userId !== userId) {
      return HttpResponse.json({ status: 'error', message: 'Forbidden' }, { status: 403 })
    }
    mockMessages.splice(index, 1)
    return HttpResponse.json(wrap(null))
  }),

  http.get('/calendar-service/v1/calendars', async ({ request }) => {
    await delay(120)
    const url = new URL(request.url)
    const year = url.searchParams.get('year')
    const month = url.searchParams.get('month')
    const userId = request.headers.get('x-user-id') ?? 'me'
    const filtered = mockCalendars.filter((item) => {
      if (item.userId && item.userId !== userId) return false
      const date = new Date(item.eventDate)
      return String(date.getFullYear()) === year && String(date.getMonth() + 1).padStart(2, '0') === String(month).padStart(2, '0')
    })
    return HttpResponse.json(wrap(filtered))
  }),

  http.post('/calendar-service/v1/calendars', async ({ request }) => {
    await delay(120)
    const body = await request.json() as any
    const userId = request.headers.get('x-user-id') ?? 'me'
    const event = mockEvents.find((item) => item.id === body.eventId)
    const created = {
      id: `cal-${Date.now()}`,
      userId,
      eventDate: body.eventDate,
      ticketingDate: body.eventDate,
      memo: body.memo ?? '',
      eventName: event?.name ?? '새 일정',
      eventId: body.eventId,
      eventStatus: event?.status ?? 'SCHEDULED',
    }
    mockCalendars.push(created as any)
    return HttpResponse.json(wrap(created), { status: 201 })
  }),

  http.patch('/calendar-service/v1/calendars/:calendarId', async ({ params, request }) => {
    await delay(120)
    const body = await request.json() as any
    const userId = request.headers.get('x-user-id')
    const role = request.headers.get('x-user-role')
    const calendar = mockCalendars.find((item) => item.id === params.calendarId)
    if (!calendar) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    if (role !== 'ADMIN' && userId !== (calendar.userId ?? 'me')) {
      return HttpResponse.json({ status: 'error', message: 'Forbidden' }, { status: 403 })
    }
    if (typeof body.memo === 'string') calendar.memo = body.memo
    return HttpResponse.json(wrap(calendar))
  }),

  http.delete('/calendar-service/v1/calendars/:calendarId', async ({ params, request }) => {
    await delay(120)
    const userId = request.headers.get('x-user-id')
    const role = request.headers.get('x-user-role')
    const index = mockCalendars.findIndex((item) => item.id === params.calendarId)
    if (index === -1) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    const calendar = mockCalendars[index]
    if (role !== 'ADMIN' && userId !== (calendar.userId ?? 'me')) {
      return HttpResponse.json({ status: 'error', message: 'Forbidden' }, { status: 403 })
    }
    mockCalendars.splice(index, 1)
    return HttpResponse.json(wrap({}))
  }),

  http.get('/favorite-service/v1/favorites', async ({ request }) => {
    await delay(120)
    const url = new URL(request.url)
    const page = Number(url.searchParams.get('page') ?? 0)
    const size = Number(url.searchParams.get('size') ?? 10)
    const userId = request.headers.get('x-user-id') ?? 'me'
    const favorites = mockFavorites.filter((item) => item.userId === userId)
    const totalElements = favorites.length
    const totalPages = Math.max(1, Math.ceil(totalElements / Math.max(size, 1)))
    const start = Math.max(page, 0) * Math.max(size, 1)
    const content = favorites.slice(start, start + Math.max(size, 1))
    return HttpResponse.json(wrap({ content, page, size, totalElements, totalPages }))
  }),

  http.post('/favorite-service/v1/favorites', async ({ request }) => {
    await delay(120)
    const body = await request.json() as any
    const userId = request.headers.get('x-user-id') ?? 'me'
    const event = mockEvents.find((item) => item.id === body.eventId)
    if (!event) {
      return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    }
    const exists = mockFavorites.find((item) => item.userId === userId && item.eventId === body.eventId)
    if (exists) {
      return HttpResponse.json({ status: 'error', message: '이미 찜한 행사입니다.' }, { status: 409 })
    }
    const created = {
      id: `fav-${Date.now()}`,
      favoriteId: `fav-${Date.now()}`,
      eventId: body.eventId,
      categoryId: body.categoryId,
      userId,
      eventName: event.name,
      eventImg: event.img,
    }
    mockFavorites.push(created as any)
    return HttpResponse.json(wrap({
      eventId: created.eventId,
      eventName: created.eventName,
      userId: created.userId,
    }), { status: 201 })
  }),

  http.delete('/favorite-service/v1/favorites/:favoriteId', async ({ params, request }) => {
    await delay(120)
    const userId = request.headers.get('x-user-id') ?? 'me'
    const index = mockFavorites.findIndex((item) => item.favoriteId === params.favoriteId && item.userId === userId)
    if (index === -1) {
      return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    }
    mockFavorites.splice(index, 1)
    return HttpResponse.json(wrap({}))
  }),

  http.get('/user-service/v1/users/admin', async ({ request }) => {
    await delay(180)
    const url = new URL(request.url)
    const email = (url.searchParams.get('email') ?? '').trim().toLowerCase()
    const name = (url.searchParams.get('name') ?? '').trim().toLowerCase()
    const role = (url.searchParams.get('role') ?? '').trim().toUpperCase()
    const page = Number(url.searchParams.get('page') ?? 0)
    const size = Number(url.searchParams.get('size') ?? 20)
    let users = [...mockAdminUsers]
    const hasRoleFilter = Boolean(role && role !== 'ALL')
    if (email || name) {
      users = users.filter((item) => {
        const emailMatch = email ? item.email.toLowerCase().includes(email) : true
        const nameMatch = name ? item.name.toLowerCase().includes(name) || item.nickname.toLowerCase().includes(name) : true
        return emailMatch && nameMatch
      })
    }
    if (hasRoleFilter) {
      users = users.filter((item) => item.role === role)
    }
    const totalElements = users.length
    const totalPages = Math.max(1, Math.ceil(totalElements / Math.max(size, 1)))
    const start = Math.max(page, 0) * Math.max(size, 1)
    const content = users.slice(start, start + Math.max(size, 1))
    return HttpResponse.json(wrap({ content, page, size, totalElements, totalPages }))
  }),

  http.get('/user-service/v1/users/admin/:userId', async ({ params }) => {
    await delay(160)
    const user = mockAdminUsers.find((item) => item.userId === params.userId)
    if (!user) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    return HttpResponse.json(wrap({
      userId: user.userId,
      email: user.email,
      nickname: user.nickname,
      name: user.name,
      phoneNumber: user.phoneNumber,
      role: user.role,
      status: user.status,
      createdAt: user.createdAt,
      updatedAt: user.updatedAt,
    }))
  }),

  http.patch('/user-service/v1/users/admin/:userId/role', async ({ params, request }) => {
    await delay(180)
    const body = await request.json() as any
    const user = mockAdminUsers.find((item) => item.userId === params.userId)
    if (!user) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    user.role = body.role ?? user.role
    user.updatedAt = new Date().toISOString()
    return HttpResponse.json(wrap(user))
  }),

  http.get('/operation-service/v1/blacklists', async ({ request }) => {
    await delay(180)
    const url = new URL(request.url)
    const status = (url.searchParams.get('status') ?? '').trim().toUpperCase()
    const page = Number(url.searchParams.get('page') ?? 0)
    const size = Number(url.searchParams.get('size') ?? 20)
    let blacklists = [...mockBlacklists]
    if (status && status !== 'ALL') {
      blacklists = blacklists.filter((item) => item.status === status)
    }
    const totalElements = blacklists.length
    const totalPages = Math.max(1, Math.ceil(totalElements / Math.max(size, 1)))
    const start = Math.max(page, 0) * Math.max(size, 1)
    const content = blacklists.slice(start, start + Math.max(size, 1)).map(({ id, userId, status }) => ({ id, userId, status }))
    return HttpResponse.json(wrap({ content, page, size, totalElements, totalPages }))
  }),

  http.post('/operation-service/v1/blacklists', async ({ request }) => {
    await delay(180)
    const body = await request.json() as any
    const userId = String(body.userId ?? '').trim()
    const reason = String(body.reason ?? '').trim()
    if (!userId || !reason) {
      return HttpResponse.json({ status: 'error', message: 'Invalid request' }, { status: 400 })
    }
    const existing = mockBlacklists.find((item) => item.userId === userId && item.status === 'ACTIVE')
    if (existing) {
      return HttpResponse.json({ status: 'error', message: '이미 차단된 사용자입니다.' }, { status: 409 })
    }
    const created = {
      id: `bl-${crypto.randomUUID()}`,
      userId,
      status: 'ACTIVE',
      reason,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    }
    mockBlacklists.unshift(created as any)
    syncMockUserStatus(userId, 'BLOCKED')
    return HttpResponse.json(wrap({ id: created.id, userId: created.userId, status: created.status }), { status: 201 })
  }),

  http.patch('/operation-service/v1/blacklists/:blacklistId/status', async ({ params, request }) => {
    await delay(180)
    const body = await request.json() as any
    const reason = String(body.reason ?? '').trim()
    const blacklist = mockBlacklists.find((item) => item.id === params.blacklistId) as any
    if (!blacklist) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    if (!reason) {
      return HttpResponse.json({ status: 'error', message: 'Invalid request' }, { status: 400 })
    }
    blacklist.status = 'INACTIVE'
    blacklist.reason = reason
    blacklist.updatedAt = new Date().toISOString()
    syncMockUserStatus(blacklist.userId, 'ACTIVE')
    return HttpResponse.json(wrap({ id: blacklist.id, userId: blacklist.userId, status: blacklist.status }))
  }),

  http.post('/user-service/v1/auth/login', async ({ request }) => {
    await delay(350)
    const body = await request.json() as any
    const email = String(body.email ?? '').toLowerCase()
    const isAdmin = email.includes('admin')
    const isManager = !isAdmin && email.includes('manager')
    const token = isAdmin ? 'mock-admin-token' : isManager ? 'mock-manager-token' : 'mock-token'
    const refreshToken = isAdmin ? 'mock-admin-refresh' : isManager ? 'mock-manager-refresh' : 'mock-refresh'
    if (!mockProfiles.has(token)) {
      mockProfiles.set(token, {
        userId: isAdmin ? 'admin-user' : isManager ? 'manager-user' : 'me',
        nickname: isAdmin ? '관리자' : isManager ? '매니저' : '테스트유저',
        email: isAdmin ? 'admin@festie.kr' : isManager ? 'manager@festie.kr' : 'test@festie.kr',
        name: isAdmin ? '관리자' : isManager ? '매니저' : '테스트',
        phoneNumber: isAdmin ? '010-9999-9999' : isManager ? '010-8888-8888' : '010-1234-5678',
        role: isAdmin ? 'ADMIN' : isManager ? 'MANAGER' : 'USER',
      })
    }
    return HttpResponse.json(wrap({
      accessToken: token,
      refreshToken,
      tokenType: 'Bearer',
    }))
  }),

  http.get('/user-service/v1/users/me', async ({ request }) => {
    await delay(150)
    const token = request.headers.get('authorization') ?? ''
    const profile = mockProfiles.get(token.replace('Bearer ', ''))
    if (!profile) {
      return HttpResponse.json(wrap({
        userId: 'me',
        nickname: '테스트유저',
        email: 'test@festie.kr',
        name: '테스트',
        phoneNumber: '010-1234-5678',
        role: 'USER',
      }))
    }
    return HttpResponse.json(wrap(profile))
  }),

  http.patch('/user-service/v1/users/me', async ({ request }) => {
    await delay(200)
    const body = await request.json() as any
    const token = request.headers.get('authorization') ?? ''
    const profile = mockProfiles.get(token.replace('Bearer ', '')) ?? {
      userId: 'me',
      nickname: '테스트유저',
      email: 'test@festie.kr',
      name: '테스트',
      phoneNumber: '010-1234-5678',
      role: 'USER' as const,
    }
    const updated = {
      ...profile,
      name: body.name ?? profile.name,
      nickname: body.nickname ?? profile.nickname,
      phoneNumber: body.phoneNumber ?? profile.phoneNumber,
    }
    mockProfiles.set(token.replace('Bearer ', ''), updated)
    return HttpResponse.json(wrap(updated))
  }),

  http.post('/ai-service/v1/chatbot', async ({ request }) => {
    await delay(220)
    const body = await request.json() as any
    const question = String(body.question ?? '').trim()
    if (!question) {
      return HttpResponse.json({ status: 'error', message: '질문을 입력해주세요.' }, { status: 400 })
    }
    return HttpResponse.json(wrap({
      answer: buildChatbotReply(question),
    }))
  }),

  http.post('/user-service/v1/users', async () => {
    await delay(350)
    return HttpResponse.json(wrap({ userId: 'new-user' }), { status: 201 })
  }),

  http.post('/user-service/v1/auth/logout', async () => HttpResponse.json(wrap({}))),
]
