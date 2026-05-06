package com.ojosama.chatservice.config;

import java.security.Principal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Component
@RequiredArgsConstructor
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(
            org.springframework.http.server.ServerHttpRequest request,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        // CustomHandshakeHandler 가 Principal 생성
        // MessageWsController는 Principal.getName()으로 userId 사용
        Object userId = attributes.get(WebSocketAuthInterceptor.ATTR_USER_ID);
        if (userId == null) {
            return null;
        }
        return () -> userId.toString();
    }
}
