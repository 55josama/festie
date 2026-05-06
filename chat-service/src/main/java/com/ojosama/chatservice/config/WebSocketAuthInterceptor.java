package com.ojosama.chatservice.config;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Slf4j
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String USER_NICKNAME_HEADER = "X-User-Nickname";

    public static final String ATTR_USER_ID = "wsUserId";
    public static final String ATTR_USER_ROLE = "wsUserRole";
    public static final String ATTR_USER_EMAIL = "wsUserEmail";
    public static final String ATTR_USER_NICKNAME = "wsUserNickname";

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        // handshake 시점에 X-User-Id, X-User-Role 읽기
        String userId = request.getHeaders().getFirst(USER_ID_HEADER);
        String role = request.getHeaders().getFirst(USER_ROLE_HEADER);
        String email = request.getHeaders().getFirst(USER_EMAIL_HEADER);
        String nickname = request.getHeaders().getFirst(USER_NICKNAME_HEADER);

        if (userId == null || userId.isBlank() || role == null || role.isBlank()) {
            log.warn("웹소켓 인증 헤더가 누락되어 연결을 거절합니다. userId={}, role={}", userId, role);
            return false;
        }

        // WebSocketAuthInterceptor 가 session attributes 에 저장
        attributes.put(ATTR_USER_ID, userId.trim());
        attributes.put(ATTR_USER_ROLE, role.trim());
        if (email != null && !email.isBlank()) {
            attributes.put(ATTR_USER_EMAIL, email.trim());
        }
        if (nickname != null && !nickname.isBlank()) {
            attributes.put(ATTR_USER_NICKNAME, nickname.trim());
        }

        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // noop
    }
}
