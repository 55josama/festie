package com.ojosama.userservice.global.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "internal.api")
public record InternalApiProperties(
        String token
) {
}