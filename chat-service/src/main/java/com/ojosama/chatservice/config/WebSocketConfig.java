package com.ojosama.chatservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final String[] allowedOriginPatterns;

    public WebSocketConfig(
            @Value("${app.websocket.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*}") String[] allowedOriginPatterns
    ) {
        this.allowedOriginPatterns = allowedOriginPatterns;
    }

    /* 클라이언트가 웹 소켓 서버에 연결하는데 사용할 웹 소켓 엔드포인트 등록
     * /ws: 클라이언트가 최초 연결하는 주소
     * */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOriginPatterns)
                .withSockJS();
    }

    /* 한 클라이언트에서 다른 클라이언트로 메시지를 라우팅하는데 사용될 메시지 브로커
     * /app: 클라이언트가 서버로 보내는 prefix
     * /topic: 서버가 구독자들에게 뿌리는 prefix
     * /queue: 서버 -> 1명(개인)에게 보내는 주소에 주로 사용
     * /user: “개인 주소”라는 뜻의 prefix
     * */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }
}
