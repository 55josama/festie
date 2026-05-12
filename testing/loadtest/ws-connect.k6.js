import ws from 'k6/ws';
import {check} from 'k6';
import {Counter, Trend} from 'k6/metrics';

const ids = JSON.parse(open('../data/loadtest-ids.json'));

const wsBaseUrl = __ENV.WS_URL || 'ws://localhost:8088/ws/websocket';
const origin = __ENV.ORIGIN || 'http://localhost:3000';
const wsMode = (__ENV.WS_MODE || 'stomp').toLowerCase();
const roomMode = (__ENV.ROOM_MODE || 'distributed').toLowerCase();
const targetChatRoomId = __ENV.TARGET_CHAT_ROOM_ID || '';
const connectionHoldMs = Number(__ENV.CONNECTION_HOLD_MS || 120000);
const targetConnections = Number(__ENV.TARGET_CONNECTIONS || __ENV.TARGET_VUS || 100);
const maxDuration = __ENV.MAX_DURATION || '30m';

export const options = {
    scenarios: {
        websocket_connections: {
            executor: 'per-vu-iterations',
            vus: targetConnections,
            iterations: 1,
            maxDuration,
            exec: 'wsConnectAndHold',
        },
    },
};

const wsConnectDuration = new Trend('ws_connect_duration');
const wsConnectSuccess = new Counter('ws_connect_success');
const wsConnectFailure = new Counter('ws_connect_failure');
const wsSessionEstablished = new Counter('ws_session_established');

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

function pickUserForVu() {
    const users = getUsers();
    return users[(__VU - 1) % users.length];
}

function pickRoom() {
    const rooms = getRooms();

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

function buildWsUrl(user, room) {
    const query = [
        `userId=${encodeURIComponent(user.userId)}`,
        `role=${encodeURIComponent(user.role || 'USER')}`,
    ];

    if (user.email) {
        query.push(`email=${encodeURIComponent(user.email)}`);
    }

    if (room?.chatRoomId) {
        query.push(`roomId=${encodeURIComponent(room.chatRoomId)}`);
    }

    return `${wsBaseUrl}?${query.join('&')}`;
}

function stompFrame(command, headers = {}, body = '') {
    const headerLines = Object.entries(headers).map(([key, value]) => `${key}:${value}`);
    const lines = [command, ...headerLines, '', body];
    return `${lines.join('\n')}\u0000`;
}

export function wsConnectAndHold() {
    const user = pickUserForVu();
    const room = pickRoom();
    const socketUrl = buildWsUrl(user, room);

    const startedAt = Date.now();
    let sessionEstablished = wsMode === 'raw';

    const response = ws.connect(
        socketUrl,
        {
            headers: {
                Origin: origin,
            },
        },
        (socket) => {
            socket.on('open', () => {
                socket.setTimeout(() => socket.close(), connectionHoldMs);

                if (wsMode === 'stomp') {
                    socket.send(
                        stompFrame('CONNECT', {
                            'accept-version': '1.2',
                            'heart-beat': '0,0',
                            host: 'localhost',
                        })
                    );
                }
            });

            socket.on('message', (message) => {
                const text = String(message || '');

                if (wsMode !== 'stomp' || sessionEstablished) {
                    return;
                }

                if (text.startsWith('CONNECTED')) {
                    sessionEstablished = true;
                    wsSessionEstablished.add(1);

                    socket.send(
                        stompFrame('SUBSCRIBE', {
                            id: `room-${__VU}-${__ITER}`,
                            destination: `/topic/rooms/${room.chatRoomId}/messages`,
                        })
                    );
                    socket.send(
                        stompFrame('SUBSCRIBE', {
                            id: `errors-${__VU}-${__ITER}`,
                            destination: '/user/queue/errors',
                        })
                    );
                }
            });
        }
    );

    const duration = Date.now() - startedAt;
    wsConnectDuration.add(duration);

    const handshakeOk = check(response, {
        'ws handshake status is 101': (res) => res && res.status === 101,
    });

    const heldLongEnough = duration >= Math.max(500, connectionHoldMs * 0.9);
    if (handshakeOk && sessionEstablished && heldLongEnough) {
        wsConnectSuccess.add(1);
    } else {
        wsConnectFailure.add(1);
    }
}
