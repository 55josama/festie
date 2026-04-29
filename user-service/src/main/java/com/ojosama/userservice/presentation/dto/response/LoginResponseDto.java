package com.ojosama.userservice.presentation.dto.response;

import com.ojosama.userservice.application.dto.result.LoginResult;

public record LoginResponseDto(
        String accessToken,
        String refreshToken,
        String tokenType
) {
    public static LoginResponseDto from(LoginResult result) {
        return new LoginResponseDto(
                result.accessToken(),
                result.refreshToken(),
                "Bearer"
        );
    }
}