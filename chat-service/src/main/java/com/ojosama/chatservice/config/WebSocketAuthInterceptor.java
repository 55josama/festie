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

    public static final String ATTR_USER_ID = "wsUserId";
    public static final String ATTR_USER_ROLE = "wsUserRole";
    public static final String ATTR_USER_EMAIL = "wsUserEmail";

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String userId = firstNonBlank(
                request.getHeaders().getFirst(USER_ID_HEADER),
                queryParam(request, "userId")
        );
        String role = firstNonBlank(
                request.getHeaders().getFirst(USER_ROLE_HEADER),
                queryParam(request, "role")
        );
        String email = firstNonBlank(
                request.getHeaders().getFirst(USER_EMAIL_HEADER),
                queryParam(request, "email")
        );

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

    private String queryParam(ServerHttpRequest request, String name) {
        return org.springframework.web.util.UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst(name);
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        if (second != null && !second.isBlank()) {
            return second.trim();
        }
        return null;
    }
}
