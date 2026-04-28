package com.ojosama.userservice.global.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(

        @NotBlank(message = "JWT Secret은 필수입니다.")
        @Size(min = 32, message = "JWT Secret은 최소 32자 이상이어야 합니다.")
        String secret,

        @Positive(message = "Access Token 만료 시간은 0보다 커야 합니다.")
        long accessTokenExpiration,

        @Positive(message = "Refresh Token 만료 시간은 0보다 커야 합니다.")
        long refreshTokenExpiration
) {
}