package com.ojosama.gatewayserver.global.filter;

import com.ojosama.gatewayserver.global.security.JwtTokenProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationGlobalFilter implements GlobalFilter, Ordered {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ACCESS_TOKEN_QUERY_PARAM = "accessToken";

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    private final JwtTokenProvider jwtTokenProvider;

    private static final List<PublicEndpoint> PUBLIC_ENDPOINTS = List.of(
            new PublicEndpoint(HttpMethod.POST, "/user-service/v1/auth/login"),
            new PublicEndpoint(HttpMethod.POST, "/user-service/v1/auth/reissue"),
            new PublicEndpoint(HttpMethod.POST, "/user-service/v1/users"),
            new PublicEndpoint(HttpMethod.GET, "/chat-service/ws-test.html"),
            new PublicEndpoint(HttpMethod.GET, "/event-service/v1/events"),
            new PublicEndpoint(HttpMethod.GET, "/event-service/v1/events/**"),
            new PublicEndpoint(HttpMethod.GET, "/community-service/v1/community-categories"),
            new PublicEndpoint(HttpMethod.GET, "/community-service/v1/posts"),
            new PublicEndpoint(HttpMethod.GET, "/community-service/v1/posts/**"),
            new PublicEndpoint(HttpMethod.GET, "/chat-service/v1/chat/rooms/event"),
            new PublicEndpoint(HttpMethod.GET, "/chat-service/v1/chat/rooms/popular"),
            new PublicEndpoint(HttpMethod.GET, "/chat-service/v1/chat/rooms/**"),

            // Swagger
            new PublicEndpoint(HttpMethod.GET, "/swagger-ui.html"),
            new PublicEndpoint(HttpMethod.GET, "/swagger-ui/**"),
            new PublicEndpoint(HttpMethod.GET, "/v3/api-docs/**"),
            new PublicEndpoint(HttpMethod.GET, "/docs/**"),
            new PublicEndpoint(HttpMethod.GET, "/swagger-resources/**"),
            new PublicEndpoint(HttpMethod.GET, "/webjars/**"),

            // 게이트웨이 시큐리티 설정 부분
            new PublicEndpoint(HttpMethod.GET, "/ai-service/actuator/prometheus"),
            new PublicEndpoint(HttpMethod.GET, "/calendar-service/actuator/prometheus"),
            new PublicEndpoint(HttpMethod.GET, "/chat-service/actuator/prometheus"),
            new PublicEndpoint(HttpMethod.GET, "/community-service/actuator/prometheus"),
            new PublicEndpoint(HttpMethod.GET, "/event-service/actuator/prometheus"),
            new PublicEndpoint(HttpMethod.GET, "/favorite-service/actuator/prometheus"),
            new PublicEndpoint(HttpMethod.GET, "/notification-service/actuator/prometheus"),
            new PublicEndpoint(HttpMethod.GET, "/operation-service/actuator/prometheus"),
            new PublicEndpoint(HttpMethod.GET, "/user-service/actuator/prometheus")
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        if (HttpMethod.OPTIONS.equals(method) || isPublicEndpoint(method, path)) {
            return chain.filter(exchange);
        }

        String authorizationHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(AUTHORIZATION_HEADER);
        String token = resolveToken(exchange, authorizationHeader);

        if (token == null || token.isBlank()) {
            return unauthorized(exchange);
        }

        if (!jwtTokenProvider.validateAccessToken(token)) {
            return unauthorized(exchange);
        }

        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .headers(headers -> {
                    headers.remove(USER_ID_HEADER);
                    headers.remove(USER_EMAIL_HEADER);
                    headers.remove(USER_ROLE_HEADER);
                })
                .header(USER_ID_HEADER, jwtTokenProvider.getUserId(token))
                .header(USER_EMAIL_HEADER, jwtTokenProvider.getEmail(token))
                .header(USER_ROLE_HEADER, jwtTokenProvider.getRole(token))
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private String resolveToken(ServerWebExchange exchange, String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return normalizeToken(authorizationHeader.substring(BEARER_PREFIX.length()));
        }

        if (isWebSocketEndpoint(exchange.getRequest().getURI().getPath())) {
            String queryToken = UriComponentsBuilder.fromUri(exchange.getRequest().getURI())
                    .build()
                    .getQueryParams()
                    .getFirst(ACCESS_TOKEN_QUERY_PARAM);
            if (queryToken != null && !queryToken.isBlank()) {
                return normalizeToken(queryToken);
            }
        }

        return null;
    }

    private String normalizeToken(String token) {
        String normalized = token == null ? null : token.trim();
        if (normalized != null && normalized.startsWith(BEARER_PREFIX)) {
            return normalized.substring(BEARER_PREFIX.length()).trim();
        }
        return normalized;
    }

    private boolean isPublicEndpoint(HttpMethod method, String path) {
        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(endpoint -> endpoint.matches(method, path));
    }

    private boolean isWebSocketEndpoint(String path) {
        return path != null && path.startsWith("/chat-service/ws");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("X-Error-Source", "gateway");
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -80;
    }

    private record PublicEndpoint(
            HttpMethod method,
            String path
    ) {
        private boolean matches(HttpMethod requestMethod, String requestPath) {
            if (!method.equals(requestMethod)) {
                return false;
            }
            if (path.endsWith("/**")) {
                String prefix = path.substring(0, path.length() - 3);
                return requestPath.equals(prefix) || requestPath.startsWith(prefix + "/");
            }
            return path.equals(requestPath);
        }
    }
}
