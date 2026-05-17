export const mockEvents = [
  {
    id: '1', name: '2026 봄 콘서트', categoryId: 'c1', categoryName: '콘서트',
    startAt: '2026-05-01T18:00:00', endAt: '2026-05-02T22:00:00',
    place: '올림픽공원 체조경기장', region: '서울', minFee: 132000, maxFee: 165000,
    hasTicketing: true, ticketingOpenAt: '2026-04-25T20:00:00',
    ticketingLink: 'https://interpark.com', status: 'SCHEDULED',
    performer: 'OOO, XXX', description: '2026년 봄을 맞아 진행되는 특별 콘서트입니다.',
    img: null, liked: false, chatRoomId: 'room-1',
  },
  {
    id: '2', name: '서울 봄꽃 축제', categoryId: 'c2', categoryName: '축제',
    startAt: '2026-04-25T10:00:00', endAt: '2026-04-28T21:00:00',
    place: '여의도 한강공원', region: '서울', minFee: 0, maxFee: 0,
    hasTicketing: false, status: 'IN_PROGRESS',
    description: '여의도 한강공원에서 펼쳐지는 봄꽃 축제입니다.',
    img: null, liked: true, chatRoomId: 'room-2',
  },
  {
    id: '3', name: '아이돌 팬미팅 2026', categoryId: 'c3', categoryName: '팬미팅',
    startAt: '2026-05-10T15:00:00', endAt: '2026-05-10T19:00:00',
    place: 'KSPO DOME', region: '서울', minFee: 110000, maxFee: 150000,
    hasTicketing: true, ticketingOpenAt: '2026-05-01T20:00:00',
    status: 'SCHEDULED', performer: '아이돌그룹',
    description: '2026년 첫 번째 팬미팅!', img: null, liked: false, chatRoomId: 'room-3',
  },
  {
    id: '4', name: '브랜드 팝업 성수', categoryId: 'c4', categoryName: '팝업스토어',
    startAt: '2026-04-20T11:00:00', endAt: '2026-05-05T20:00:00',
    place: '성수동 팝업공간', region: '서울', minFee: 0, maxFee: 0,
    hasTicketing: false, status: 'IN_PROGRESS',
    description: '성수동에서 만나는 특별한 브랜드 팝업스토어.',
    img: null, liked: false, chatRoomId: 'room-4',
  },
  {
    id: '5', name: '서머 페스티벌', categoryId: 'c1', categoryName: '콘서트',
    startAt: '2026-06-15T17:00:00', endAt: '2026-06-15T23:00:00',
    place: '잠실종합운동장', region: '서울', minFee: 99000, maxFee: 99000,
    hasTicketing: true, ticketingOpenAt: '2026-05-16T20:00:00',
    status: 'SCHEDULED', img: null, liked: false, chatRoomId: 'room-5',
  },
  {
    id: '6', name: '드림 팬미팅', categoryId: 'c3', categoryName: '팬미팅',
    startAt: '2026-06-20T14:00:00', endAt: '2026-06-20T18:00:00',
    place: '고척스카이돔', region: '서울', minFee: 130000, maxFee: 200000,
    hasTicketing: true, ticketingOpenAt: '2026-05-20T20:00:00',
    status: 'SCHEDULED', performer: '드림팀',
    img: null, liked: true, chatRoomId: 'room-6',
  },
]

export const mockEventCategories = [
  { id: 'c1', name: '콘서트' },
  { id: 'c2', name: '축제' },
  { id: 'c3', name: '팬미팅' },
  { id: 'c4', name: '팝업스토어' },
]

export const mockCategories = [
  { id: 'cat1', name: '후기' },
  { id: 'cat2', name: '꿀팁' },
  { id: 'cat3', name: '자유' },
  { id: 'cat4', name: '요청' },
]

export const mockPosts = [
  { id: '1', userId: 'u1', categoryId: 'cat1', categoryName: '후기', title: '2026 봄 콘서트 1일차 후기 진짜 너무 좋았어요', content: '좌석 시야도 좋았고, 무대 구성도 깔끔했어요. 다음 회차도 가고 싶습니다.', viewCount: 320, likeCount: 87, commentCount: 23, status: 'CLEAN', createdAt: '2026-05-13T17:55:00', updatedAt: '2026-05-13T18:10:00', authorNickname: '페스티유저', eventName: '2026 봄 콘서트' },
  { id: '2', userId: 'u2', categoryId: 'cat2', categoryName: '꿀팁', title: '올림픽공원 주차 꿀팁 총정리 P3 P4 비교해봤습니다', content: '주차장마다 동선이 달라서 미리 확인하면 훨씬 편합니다.', viewCount: 210, likeCount: 41, commentCount: 12, status: 'CLEAN', createdAt: '2026-05-13T17:00:00', updatedAt: '2026-05-13T17:20:00', authorNickname: '콘서트고수', eventName: '2026 봄 콘서트' },
  { id: '3', userId: 'u3', categoryId: 'cat3', categoryName: '자유', title: '서울 봄꽃 축제 같이 갈 사람 구해요', content: '주말에 여의도 가실 분 있으면 댓글 주세요.', viewCount: 88, likeCount: 12, commentCount: 8, status: 'CLEAN', createdAt: '2026-05-13T16:00:00', updatedAt: '2026-05-13T16:15:00', authorNickname: '봄봄봄', eventName: '서울 봄꽃 축제' },
  { id: '4', userId: 'u4', categoryId: 'cat4', categoryName: '요청', title: '성수 팝업스토어 등록 요청드립니다', content: '공식 인스타 링크와 위치를 함께 남깁니다. 확인 부탁드려요.', viewCount: 45, likeCount: 3, commentCount: 1, status: 'CLEAN', createdAt: '2026-05-13T15:00:00', updatedAt: '2026-05-13T15:10:00', authorNickname: '팝업덕후' },
  { id: '5', userId: 'u5', categoryId: 'cat1', categoryName: '후기', title: '드림 팬미팅 굿즈 구성 미리보기 공유해요', content: '현장 굿즈 사진 정리했습니다. 입장 전 확인하면 좋아요.', viewCount: 480, likeCount: 56, commentCount: 19, status: 'CLEAN', createdAt: '2026-05-13T13:00:00', updatedAt: '2026-05-13T13:20:00', authorNickname: '팬심가득', eventName: '드림 팬미팅' },
]

export const mockMessages = [
  { messageId: '1', chatRoomId: 'room-1', eventId: '1', userId: 'u1', writerNickname: '페스티유저', messageType: 'USER', content: '줄이 엄청 길어요 ㅠㅠ', status: 'ACTIVE', createdAt: '2026-05-01T17:32:00' },
  { messageId: '2', chatRoomId: 'room-1', eventId: '1', userId: 'u2', writerNickname: '콘서트고수', messageType: 'USER', content: '저는 방금 입장했어요! 빠르게 오세요', status: 'ACTIVE', createdAt: '2026-05-01T17:33:00' },
  { messageId: '3', chatRoomId: 'room-1', eventId: '1', userId: 'system', writerNickname: '', messageType: 'SYSTEM', content: '채팅방이 오픈되었습니다', status: 'ACTIVE', createdAt: '2026-05-01T17:33:30' },
  { messageId: '4', chatRoomId: 'room-1', eventId: '1', userId: 'me', writerNickname: '나', messageType: 'USER', content: '굿즈 어디서 팔아요?', status: 'ACTIVE', createdAt: '2026-05-01T17:34:00' },
  { messageId: '5', chatRoomId: 'room-1', eventId: '1', userId: 'u3', writerNickname: '팬심가득', messageType: 'USER', content: '2번 게이트 앞쪽에 있어요!', status: 'ACTIVE', createdAt: '2026-05-01T17:34:30' },
]

export const mockChatRooms = [
  { chatRoomId: 'room-1', eventId: '1', eventName: '2026 봄 콘서트', category: 'CONCERT', status: 'OPEN', scheduledOpenAt: '2026-04-25T20:00:00', scheduledCloseAt: '2026-05-02T22:00:00', openedAt: '2026-04-25T20:00:00', closedAt: null as string | null, currentViewerCount: 142 },
  { chatRoomId: 'room-2', eventId: '2', eventName: '서울 봄꽃 축제', category: 'FESTIVAL', status: 'OPEN', scheduledOpenAt: '2026-04-20T10:00:00', scheduledCloseAt: '2026-04-28T21:00:00', openedAt: '2026-04-20T10:00:00', closedAt: null as string | null, currentViewerCount: 93 },
  { chatRoomId: 'room-3', eventId: '3', eventName: '아이돌 팬미팅 2026', category: 'FANMEETING', status: 'SCHEDULED', scheduledOpenAt: '2026-05-01T20:00:00', scheduledCloseAt: '2026-05-10T19:00:00', openedAt: null as string | null, closedAt: null as string | null, currentViewerCount: 0 },
  { chatRoomId: 'room-4', eventId: '4', eventName: '브랜드 팝업 성수', category: 'POPUP', status: 'OPEN', scheduledOpenAt: '2026-04-20T11:00:00', scheduledCloseAt: '2026-05-05T20:00:00', openedAt: '2026-04-20T11:00:00', closedAt: null as string | null, currentViewerCount: 61 },
  { chatRoomId: 'room-5', eventId: '5', eventName: '서머 페스티벌', category: 'CONCERT', status: 'SCHEDULED', scheduledOpenAt: '2026-05-16T20:00:00', scheduledCloseAt: '2026-06-15T23:00:00', openedAt: null as string | null, closedAt: null as string | null, currentViewerCount: 18 },
  { chatRoomId: 'room-6', eventId: '6', eventName: '드림 팬미팅', category: 'FANMEETING', status: 'SCHEDULED', scheduledOpenAt: '2026-05-20T20:00:00', scheduledCloseAt: '2026-06-20T18:00:00', openedAt: null as string | null, closedAt: null as string | null, currentViewerCount: 9 },
]

export const mockEventRequests = [
  {
    id: 'req-1',
    requesterId: 'u9',
    requesterNickname: '봄봄봄',
    createdAt: '2026-05-15T09:10:00.000Z',
    eventName: '2026 여름 콘서트',
    categoryId: 'c1',
    category: '콘서트',
    link: 'https://ticket.example.com/summer',
    description: '서울 잠실에서 열릴 예정인 여름 콘서트입니다.',
    rejectReason: null,
    status: 'PENDING',
    createdEventId: null as string | null,
  },
  {
    id: 'req-2',
    requesterId: 'u10',
    requesterNickname: '팝업덕후',
    createdAt: '2026-05-15T09:20:00.000Z',
    eventName: '성수 브랜드 팝업 2차',
    categoryId: 'c4',
    category: '팝업스토어',
    link: 'https://instagram.com/popup',
    description: '성수동에 새로 열리는 브랜드 팝업스토어 요청입니다.',
    rejectReason: null,
    status: 'APPROVED',
    createdEventId: null as string | null,
  },
]

export const mockOperationRequests = [
  {
    id: 'op-1',
    requesterId: 'u7',
    title: '메인 배너 문구 수정 요청',
    content: '홈 화면 행사 탐색 문구를 조금 더 짧게 수정하고 싶습니다.',
    status: 'PENDING',
    adminMemo: null as string | null,
  },
  {
    id: 'op-2',
    requesterId: 'u8',
    title: '채팅방 공지 고정 요청',
    content: '드림 팬미팅 채팅방 상단 공지를 고정해 주시면 좋겠습니다.',
    status: 'IN_PROGRESS',
    adminMemo: '운영팀 확인 중입니다.',
  },
]

export const mockReports = [
  {
    id: 'report-1',
    reporterId: 'u1',
    reporterType: 'USER',
    targetId: 'm-3',
    targetUserId: 'u3',
    targetType: 'CHAT',
    category: 'PROFANITY',
    description: '욕설 및 비방',
    targetContent: '줄이 엄청 길어요 ㅠㅠ',
    status: 'AUTO_BLINDED',
    operatorMemo: null,
  },
  {
    id: 'report-2',
    reporterId: 'u2',
    reporterType: 'USER',
    targetId: 'c1',
    targetUserId: 'u2',
    targetType: 'COMMENT',
    category: 'UNAUTHORIZED_TRADE',
    description: '티켓 재판매 유도',
    targetContent: '시야가 좋았다니 기대되네요!',
    status: 'RESOLVED',
    operatorMemo: '게시물 삭제 및 계정 경고 처리',
  },
]

export const mockBlacklists = [
  {
    id: 'bl-1',
    userId: 'u-1002',
    status: 'ACTIVE',
  },
  {
    id: 'bl-2',
    userId: 'u-1001',
    status: 'INACTIVE',
  },
]

export const mockComments = [
  { id: 'c1', postId: '1', userId: 'u2', parentId: null, content: '시야가 좋았다니 기대되네요!', status: 'CLEAN', likeCount: 4, createdAt: '2026-05-13T18:20:00', updatedAt: '2026-05-13T18:20:00', replies: [] },
  { id: 'c2', postId: '1', userId: 'u3', parentId: null, content: '사진도 올려주시면 더 좋을 것 같아요.', status: 'CLEAN', likeCount: 2, createdAt: '2026-05-13T18:30:00', updatedAt: '2026-05-13T18:30:00', replies: [] },
]

export const mockCalendars = [
  { id: 'cal-1', userId: 'me', eventDate: '2026-05-01T18:00:00', ticketingDate: '2026-04-25T20:00:00', memo: '1일차 입장 30분 전에 도착', eventName: '2026 봄 콘서트', eventId: '1', eventStatus: 'SCHEDULED' },
  { id: 'cal-2', userId: 'me', eventDate: '2026-05-10T15:00:00', ticketingDate: '2026-05-01T20:00:00', memo: '친구랑 같이 가기', eventName: '아이돌 팬미팅 2026', eventId: '3', eventStatus: 'SCHEDULED' },
]

export const mockFavorites = [
  {
    id: 'fav-1',
    favoriteId: 'fav-1',
    eventId: '1',
    categoryId: 'c1',
    userId: 'me',
    eventName: '2026 봄 콘서트',
    eventImg: null,
  },
  {
    id: 'fav-2',
    favoriteId: 'fav-2',
    eventId: '4',
    categoryId: 'c4',
    userId: 'me',
    eventName: '브랜드 팝업 성수',
    eventImg: null,
  },
]

export const mockNotifications = [
  {
    id: 'nt-1',
    title: '새 행사 알림',
    content: '2026 불빛축제가 곧 시작돼요.',
    readAt: null as string | null,
  },
  {
    id: 'nt-2',
    title: '행사 요청 결과',
    content: '요청하신 행사 등록이 승인되었습니다.',
    readAt: '2026-05-16T09:30:00.000Z',
  },
  {
    id: 'nt-3',
    title: '블랙리스트 알림',
    content: '신고한 유저가 관리자 검토 대상에 올랐습니다.',
    readAt: null as string | null,
  },
]

export const mockNotices = [
  {
    noticeId: 'notice-1',
    adminId: 'admin-user',
    title: 'Festie 서비스 점검 안내',
    content: '5월 20일 새벽 2시부터 3시까지 서비스 점검이 예정되어 있습니다. 점검 시간 동안 일부 기능이 제한될 수 있습니다.',
  },
  {
    noticeId: 'notice-2',
    adminId: 'admin-user',
    title: '운영 요청 처리 정책 변경 안내',
    content: '운영 요청 승인/반려 흐름이 일부 조정되었습니다. 요청 상태는 요청 상세에서 확인해 주세요.',
  },
]
