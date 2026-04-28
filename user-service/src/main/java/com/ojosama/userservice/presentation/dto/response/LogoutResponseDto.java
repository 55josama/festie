package com.ojosama.userservice.presentation.dto.response;

import com.ojosama.userservice.application.dto.result.LogoutResult;
import java.util.UUID;

public record LogoutResponseDto(
        UUID userId,
        String message
) {
    public static LogoutResponseDto from(LogoutResult result) {
        return new LogoutResponseDto(
                result.userId(),
                "로그아웃이 완료되었습니다."
        );
    }
}