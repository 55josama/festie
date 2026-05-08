package com.ojosama.chatservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Component
@RequiredArgsConstructor
public class ChatRoomPopularityEventListener {
    // 웹소켓에서 뭔가 바뀌면 여기로 들어옴
    private final ChatRoomPopularityTracker popularityTracker;

    // 방 구독 시작
    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        popularityTracker.markSubscribed(
                accessor.getSessionId(),
                accessor.getSubscriptionId(),
                accessor.getDestination()
        );
    }

    // 방 구독 취소
    @EventListener
    public void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        popularityTracker.markUnsubscribed(
                accessor.getSessionId(),
                accessor.getSubscriptionId()
        );
    }

    // 연결이 끊기면 세션 값 다 지움
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        popularityTracker.clearSession(event.getSessionId());
    }
}
