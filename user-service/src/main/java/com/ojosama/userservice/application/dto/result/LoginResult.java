package com.ojosama.userservice.application.dto.result;

public record LoginResult(
        String accessToken,
        String refreshToken
) {
}