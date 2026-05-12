import ws from 'k6/ws';
import { check, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';

const ids = JSON.parse(open('../data/loadtest-ids.json'));

const wsBaseUrl = __ENV.WS_URL || 'ws://localhost:8088/ws/websocket';
const origin = __ENV.ORIGIN || 'http://localhost:3000';
const roomMode = (__ENV.ROOM_MODE || 'distributed').toLowerCase();
const sendMode = (__ENV.SEND_MODE || 'single').toLowerCase();
const targetChatRoomId = __ENV.TARGET_CHAT_ROOM_ID || '';
const targetConnections = Number(__ENV.TARGET_CONNECTIONS || __ENV.TARGET_VUS || 10);
const maxDuration = __ENV.MAX_DURATION || '30m';
const connectionHoldMs = Number(__ENV.CONNECTION_HOLD_MS || 15000);
const postSendWaitMs = Number(__ENV.POST_SEND_WAIT_MS || 3000);
const burstCount = Number(__ENV.BURST_COUNT || 5);
const burstIntervalMs = Number(__ENV.BURST_INTERVAL_MS || 1000);
const messagePrefix = __ENV.MESSAGE_PREFIX || 'k6 ws send';

export const options = {
    scenarios: {
        ws_message_send: {
            executor: 'per-vu-iterations',
            vus: targetConnections,
            iterations: 1,
            maxDuration,
            exec: 'wsMessageSend',
        },
    },
};

const wsConnectDuration = new Trend('ws_connect_duration');
const wsSendDuration = new Trend('ws_send_duration');
const wsConnectSuccess = new Counter('ws_connect_success');
const wsConnectFailure = new Counter('ws_connect_failure');
const wsSessionEstablished = new Counter('ws_session_established');
const wsMessagesReceived = new Counter('ws_messages_received');

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

function sendStompMessage(socket, room, content) {
    const startedAt = Date.now();
    const payload = JSON.stringify({
        chatRoomId: room.chatRoomId,
        content,
    });

    socket.send(
        stompFrame('SEND', {
            destination: '/app/chat.send',
            'content-type': 'application/json',
        }, payload)
    );

    wsSendDuration.add(Date.now() - startedAt);
}

export function wsMessageSend() {
    const user = pickUserForVu();
    const room = pickRoom();
    const socketUrl = buildWsUrl(user, room);

    const startedAt = Date.now();
    let connected = false;
    let subscribed = false;
    let sentOnce = false;

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
                socket.send(
                    stompFrame('CONNECT', {
                        'accept-version': '1.2',
                        'heart-beat': '0,0',
                        host: 'localhost',
                    })
                );
            });

            socket.on('message', (message) => {
                const text = String(message || '');

                if (!connected && text.startsWith('CONNECTED')) {
                    connected = true;
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
                    subscribed = true;

                    sleep(0.1);

                    if (!sentOnce) {
                        if (sendMode === 'burst') {
                            for (let i = 1; i <= burstCount; i += 1) {
                                sendStompMessage(
                                    socket,
                                    room,
                                    `${messagePrefix} #${__VU}-${__ITER}-${i}`
                                );

                                if (i < burstCount) {
                                    sleep(burstIntervalMs / 1000);
                                }
                            }
                        } else {
                            sendStompMessage(
                                socket,
                                room,
                                `${messagePrefix} #${__VU}-${__ITER}`
                            );
                        }

                        sentOnce = true;
                        socket.setTimeout(() => socket.close(), postSendWaitMs);
                    }
                    return;
                }

                if (text.startsWith('MESSAGE')) {
                    wsMessagesReceived.add(1);
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
    if (handshakeOk && connected && subscribed && heldLongEnough) {
        wsConnectSuccess.add(1);
    } else {
        wsConnectFailure.add(1);
    }
}
