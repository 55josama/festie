import http from 'k6/http';
import {check, sleep} from 'k6';
import {Counter, Trend} from 'k6/metrics';

const ids = JSON.parse(open('../data/loadtest-ids.json'));

const eventServiceUrl = __ENV.EVENT_URL || 'http://localhost:8082';
const chatServiceUrl = __ENV.CHAT_URL || 'http://localhost:8088';
const roomMode = (__ENV.ROOM_MODE || 'distributed').toLowerCase();
const targetChatRoomId = __ENV.TARGET_CHAT_ROOM_ID || '';
const thinkTimeMs = Number(__ENV.THINK_TIME_MS || 200);

export const options = {
    scenarios: {
        chat_room_entry: {
            executor: 'ramping-vus',
            startVUs: Number(__ENV.START_VUS || 1),
            stages: [
                {duration: __ENV.RAMP_UP || '30s', target: Number(__ENV.TARGET_VUS || 10)},
                {duration: __ENV.HOLD || '1m', target: Number(__ENV.TARGET_VUS || 10)},
                {duration: __ENV.RAMP_DOWN || '30s', target: 0},
            ],
            gracefulRampDown: '10s',
            exec: 'chatRoomEntry',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<800'],
    },
};

const eventDuration = new Trend('event_detail_duration');
const chatRoomDuration = new Trend('chat_room_duration');
const messageListDuration = new Trend('message_list_duration');
const successCount = new Counter('success_count');
const failureCount = new Counter('failure_count');

function safeJson(response) {
    const text = response.body || '';
    if (!text) {
        return null;
    }

    try {
        return JSON.parse(text);
    } catch {
        return null;
    }
}

function unwrapData(response) {
    const parsed = safeJson(response);
    return parsed?.data ?? parsed ?? null;
}

function pickChatRoom() {
    const chatRooms = ids.chatRooms ?? [];
    if (!chatRooms.length) {
        throw new Error('data/loadtest-ids.json has no chatRooms');
    }

    if (roomMode === 'hot' && targetChatRoomId) {
        const target = chatRooms.find((room) => room.chatRoomId === targetChatRoomId);
        if (!target) {
            throw new Error(`target chat room not found: ${targetChatRoomId}`);
        }

        return target;
    }

    if (roomMode === 'hot') {
        return chatRooms[0];
    }

    const baseIndex = ((__VU - 1) + (__ITER % chatRooms.length)) % chatRooms.length;
    return chatRooms[baseIndex];
}

export function chatRoomEntry() {
    const chatRoomSeed = pickChatRoom();
    const eventId = chatRoomSeed.eventId;

    sleep(thinkTimeMs / 1000);

    const eventStart = Date.now();
    const eventRes = http.get(
        `${eventServiceUrl}/v1/events/${eventId}`,
        {tags: {api: 'event_detail'}}
    );
    eventDuration.add(Date.now() - eventStart);

    check(eventRes, {
        'event detail status is 200': (r) => r.status === 200,
        'event detail has data': (r) => Boolean(unwrapData(r)?.id),
    }) ? successCount.add(1) : failureCount.add(1);

    const eventData = unwrapData(eventRes);
    if (!eventData?.id) {
        return;
    }

    sleep(thinkTimeMs / 1000);

    const roomStart = Date.now();
    const roomRes = http.get(
        `${chatServiceUrl}/v1/chat/rooms/event?eventId=${eventData.id}`,
        {tags: {api: 'chat_room_by_event'}}
    );
    chatRoomDuration.add(Date.now() - roomStart);

    check(roomRes, {
        'chat room status is 200': (r) => r.status === 200,
        'chat room has chatRoomId': (r) => Boolean(unwrapData(r)?.chatRoomId),
    }) ? successCount.add(1) : failureCount.add(1);

    const chatRoomData = unwrapData(roomRes);
    if (!chatRoomData?.chatRoomId) {
        return;
    }

    const messagesStart = Date.now();
    const messagesRes = http.get(
        `${chatServiceUrl}/v1/chat/rooms/${chatRoomData.chatRoomId}/messages?page=0&size=20`,
        {tags: {api: 'message_list'}}
    );
    messageListDuration.add(Date.now() - messagesStart);

    check(messagesRes, {
        'message list status is 200': (r) => r.status === 200,
        'message list has data': (r) => Array.isArray(unwrapData(r)?.messages)
            && unwrapData(r).messages.length > 0,
    }) ? successCount.add(1) : failureCount.add(1);

    sleep(thinkTimeMs / 1000);
}
