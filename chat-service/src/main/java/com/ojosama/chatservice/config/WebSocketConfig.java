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
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;
    private final CustomHandshakeHandler customHandshakeHandler;

    public WebSocketConfig(
            @Value("${app.websocket.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*}") String[] allowedOriginPatterns,
            WebSocketAuthInterceptor webSocketAuthInterceptor,
            CustomHandshakeHandler customHandshakeHandler
    ) {
        this.allowedOriginPatterns = allowedOriginPatterns;
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
        this.customHandshakeHandler = customHandshakeHandler;
    }

    /* 클라이언트가 웹 소켓 서버에 연결하는데 사용할 웹 소켓 엔드포인트 등록
     * /ws: 클라이언트가 최초 연결하는 주소
     * WebSocket handshake interceptor : X-User-Id, X-User-Role 읽고 session attributes 에 저장
     * custom handshake handler : 핸드셰이크가 성공할 때 Principal 을 만들어서 연결에 붙여줌
     * */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(webSocketAuthInterceptor)
                .setHandshakeHandler(customHandshakeHandler)
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
