import http from 'k6/http';
import {check, sleep} from 'k6';
import {Counter, Trend} from 'k6/metrics';

const ids = JSON.parse(open('../data/loadtest-ids.json'));

const gatewayUrl = __ENV.GATEWAY_URL || 'http://localhost:8080';
const thinkTimeMs = Number(__ENV.THINK_TIME_MS || 200);
const roomMode = (__ENV.ROOM_MODE || 'distributed').toLowerCase();
const sendMode = (__ENV.SEND_MODE || 'single').toLowerCase();
const authMode = (__ENV.AUTH_MODE || 'setup').toLowerCase();
const burstCount = Number(__ENV.BURST_COUNT || 5);
const burstIntervalMs = Number(__ENV.BURST_INTERVAL_MS || 10000);
const targetChatRoomId = __ENV.TARGET_CHAT_ROOM_ID || '';
const messagePrefix = __ENV.MESSAGE_PREFIX || 'k6 loadtest message';
const testUserPassword = __ENV.TEST_USER_PASSWORD || '1234';

export const options = {
    scenarios: {
        message_send: {
            executor: 'ramping-vus',
            startVUs: Number(__ENV.START_VUS || 1),
            stages: [
                {duration: __ENV.RAMP_UP || '30s', target: Number(__ENV.TARGET_VUS || 10)},
                {duration: __ENV.HOLD || '1m', target: Number(__ENV.TARGET_VUS || 10)},
                {duration: __ENV.RAMP_DOWN || '30s', target: 0},
            ],
            gracefulRampDown: '10s',
            exec: 'messageSend',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<800'],
    },
};

const messageSendDuration = new Trend('message_send_duration');
const successCount = new Counter('success_count');
const failureCount = new Counter('failure_count');
let cachedAccessToken = null;
let cachedUser = null;

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

function authHeaders(token) {
    return {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
    };
}

function getUsers() {
    const users = ids.users ?? [];
    if (!users.length) {
        throw new Error('data/loadtest-ids.json has no users');
    }

    return users;
}

function getRooms() {
    const rooms = ids.chatRooms ?? [];
    if (!rooms.length) {
        throw new Error('data/loadtest-ids.json has no chatRooms');
    }

    return rooms;
}

function pickUserForVu(setupData) {
    const users = setupData?.users?.length ? setupData.users : getUsers();
    if (!users.length) {
        throw new Error('no users available');
    }

    return users[(__VU - 1) % users.length];
}

function pickRoom(setupData) {
    const rooms = setupData?.chatRooms?.length ? setupData.chatRooms : getRooms();
    if (!rooms.length) {
        throw new Error('no chatRooms available');
    }

    if (targetChatRoomId) {
        const exact = rooms.find((room) => room.chatRoomId === targetChatRoomId);
        if (!exact) {
            throw new Error(`target chat room not found: ${targetChatRoomId}`);
        }

        return exact;
    }

    if (roomMode === 'hot') {
        return rooms[0];
    }

    const baseIndex = ((__VU - 1) + (__ITER % rooms.length)) % rooms.length;
    return rooms[baseIndex];
}

function performLogin(user) {
    const loginRes = http.post(
        `${gatewayUrl}/user-service/v1/auth/login`,
        JSON.stringify({
            email: user.email,
            password: user.password || testUserPassword,
        }),
        {
            headers: {
                'Content-Type': 'application/json',
            },
            tags: {
                api: 'login',
            },
        }
    );

    check(loginRes, {
        'login status is 200': (r) => r.status === 200,
        'login has access token': (r) => Boolean(unwrapData(r)?.accessToken),
    }) ? successCount.add(1) : failureCount.add(1);

    const accessToken = unwrapData(loginRes)?.accessToken;
    if (!accessToken) {
        return null;
    }

    return accessToken;
}

function getAccessTokenForVu(user, setupData) {
    if (setupData?.tokenByUserId?.[user.userId]) {
        return setupData.tokenByUserId[user.userId];
    }

    if (cachedAccessToken && cachedUser?.userId === user.userId) {
        return cachedAccessToken;
    }

    if (authMode === 'setup') {
        throw new Error(
            `missing preloaded token for userId=${user.userId}. Run setup() auth or switch AUTH_MODE=per_vu`
        );
    }

    const accessToken = performLogin(user);
    if (!accessToken) {
        return null;
    }

    cachedAccessToken = accessToken;
    cachedUser = user;
    return accessToken;
}

export function setup() {
    const users = getUsers();
    const chatRooms = getRooms();

    if (authMode !== 'setup') {
        return {users, chatRooms};
    }

    const tokenByUserId = {};
    for (const user of users) {
        const accessToken = performLogin(user);
        if (!accessToken) {
            throw new Error(`setup login failed for userId=${user.userId}`);
        }

        tokenByUserId[user.userId] = accessToken;
    }

    return {
        users,
        chatRooms,
        tokenByUserId,
    };
}

function sendMessage(accessToken, chatRoomId, content) {
    const sendStart = Date.now();
    const sendRes = http.post(
        `${gatewayUrl}/chat-service/v1/chat/rooms/${chatRoomId}/messages`,
        JSON.stringify({
            content,
        }),
        {
            headers: authHeaders(accessToken),
            tags: {
                api: 'message_send',
            },
        }
    );
    messageSendDuration.add(Date.now() - sendStart);

    check(sendRes, {
        'message send status is 201': (r) => r.status === 201,
        'message send has messageId': (r) => Boolean(unwrapData(r)?.messageId),
    }) ? successCount.add(1) : failureCount.add(1);
}

export function messageSend(setupData) {
    const user = pickUserForVu(setupData);
    const accessToken = getAccessTokenForVu(user, setupData);
    if (!accessToken) {
        return;
    }

    const room = pickRoom(setupData);

    sleep(thinkTimeMs / 1000);

    if (sendMode === 'burst') {
        for (let i = 0; i < burstCount; i += 1) {
            sendMessage(
                accessToken,
                room.chatRoomId,
                `${messagePrefix} #${__VU}-${__ITER}-${i + 1}`
            );

            if (i < burstCount - 1) {
                sleep(burstIntervalMs / 1000);
            }
        }
    } else {
        sendMessage(
            accessToken,
            room.chatRoomId,
            `${messagePrefix} #${__VU}-${__ITER}`
        );
    }

    sleep(thinkTimeMs / 1000);
}
