package com.ojosama.gatewayserver.global.filter;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class InternalApiBlockFilter implements GlobalFilter, Ordered {

    private static final String INTERNAL_PATH_KEYWORD = "/internal/";

    @Override
    public Mono<Void> filter(
            org.springframework.web.server.ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain
    ) {
        String path = exchange.getRequest().getURI().getPath();

        if (path.contains(INTERNAL_PATH_KEYWORD)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}