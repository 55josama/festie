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
  mockReports,
} from './data'

const wrap = (data: any) => ({ status: 'success', data })

function normalizeCategoryKey(name: string) {
  if (name === '\uCF58\uC11C\uD2B8') return 'concert'
  if (name === '\uCD95\uC81C') return 'festival'
  if (name === '\uD32C\uBBF8\uD305') return 'fanmeeting'
  if (name === '\uD31D\uC5C5\uC2A4\uD1A0\uC5B4') return 'popup'
  return String(name).toLowerCase()
}

export const handlers = [
  http.get('/event-service/v1/events', async ({ request }) => {
    await delay(250)
    const url = new URL(request.url)
    const hasTicketing = url.searchParams.get('hasTicketing')
    let events = hasTicketing ? mockEvents.filter(e => e.hasTicketing) : [...mockEvents]
    return HttpResponse.json(wrap({ content: events, totalElements: events.length, totalPages: 1, size: 10, number: 0 }))
  }),

  http.get('/event-service/v1/events/:eventId', async ({ params }) => {
    await delay(150)
    const event = mockEvents.find(e => e.id === params.eventId)
    if (!event) return HttpResponse.json({ status: 'error', message: 'Not found' }, { status: 404 })
    return HttpResponse.json(wrap(event))
  }),

  http.post('/event-service/v1/events', async ({ request }) => {
    await delay(220)
    const body = await request.json() as any
    const categoryId = body.categoryId ?? ({ concert: 'c1', festival: 'c2', fanmeeting: 'c3', popup: 'c4' } as Record<string, string>)[normalizeCategoryKey(body.categoryName ?? body.category ?? '\uCF58\uC11C\uD2B8')] ?? 'c1'
    const categoryName = mockEventCategories.find((item) => item.id === categoryId)?.name ?? body.categoryName ?? body.category ?? '기타'
    const nextId = String(mockEvents.length + 1)
    const nextChatRoomId = `room-${mockChatRooms.length + 1}`
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
      id: 'new-post',
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
      targetType: body.targetType,
      category: body.category ?? 'GENERAL',
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

  http.get('/event-service/v1/event-requests', async ({ request }) => {
    await delay(180)
    const url = new URL(request.url)
    const status = url.searchParams.get('status')
    const filtered = status ? mockEventRequests.filter((item) => item.status === status) : mockEventRequests
    return HttpResponse.json(wrap({ content: filtered, totalElements: filtered.length, totalPages: 1, size: 10, number: 0 }))
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

  http.get('/operation-service/v1/reports', async ({ request }) => {
    await delay(180)
    const url = new URL(request.url)
    const status = url.searchParams.get('status')
    const filtered = status ? mockReports.filter((item) => item.status === status) : mockReports
    return HttpResponse.json(wrap({ content: filtered, totalElements: filtered.length, totalPages: 1, size: 10, number: 0 }))
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
    const room = mockChatRooms.find((item) => item.eventId === eventId) ?? mockChatRooms[0]
    return HttpResponse.json(wrap(room))
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

  http.post('/user-service/v1/auth/login', async ({ request }) => {
    await delay(350)
    const body = await request.json() as any
    const isAdmin = String(body.email ?? '').includes('admin') || String(body.email ?? '').includes('manager')
    return HttpResponse.json(wrap({
      accessToken: isAdmin ? 'mock-admin-token' : 'mock-token',
      refreshToken: isAdmin ? 'mock-admin-refresh' : 'mock-refresh',
      tokenType: 'Bearer',
    }))
  }),

  http.get('/user-service/v1/users/me', async ({ request }) => {
    await delay(150)
    const token = request.headers.get('authorization') ?? ''
    const isAdmin = token.includes('mock-admin-token')
    return HttpResponse.json(wrap({
      userId: isAdmin ? 'admin-user' : 'me',
      nickname: isAdmin ? '운영자' : '테스트유저',
      email: isAdmin ? 'admin@festie.kr' : 'test@festie.kr',
      name: isAdmin ? '관리자' : '테스트',
      phoneNumber: isAdmin ? '010-9999-9999' : '010-1234-5678',
      role: isAdmin ? 'ADMIN' : 'USER',
    }))
  }),

  http.patch('/user-service/v1/users/me', async ({ request }) => {
    await delay(200)
    const body = await request.json() as any
    const token = request.headers.get('authorization') ?? ''
    const isAdmin = token.includes('mock-admin-token')
    return HttpResponse.json(wrap({
      userId: isAdmin ? 'admin-user' : 'me',
      email: isAdmin ? 'admin@festie.kr' : 'test@festie.kr',
      name: body.name ?? (isAdmin ? '관리자' : '테스트'),
      nickname: body.nickname ?? (isAdmin ? '운영자' : '테스트유저'),
      phoneNumber: body.phoneNumber ?? (isAdmin ? '010-9999-9999' : '010-1234-5678'),
      role: isAdmin ? 'ADMIN' : 'USER',
    }))
  }),

  http.post('/user-service/v1/users', async () => {
    await delay(350)
    return HttpResponse.json(wrap({ userId: 'new-user' }), { status: 201 })
  }),

  http.post('/user-service/v1/auth/logout', async () => HttpResponse.json(wrap({}))),
]
