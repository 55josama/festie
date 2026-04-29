package com.ojosama.userservice.presentation.dto.response;

import com.ojosama.userservice.application.dto.result.UpdateUserResult;
import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateUserResponseDto(
        UUID userId,
        String email,
        String name,
        String nickname,
        String phoneNumber,
        LocalDateTime updatedAt
) {
    public static UpdateUserResponseDto from(UpdateUserResult result) {
        return new UpdateUserResponseDto(
                result.userId(),
                result.email(),
                result.name(),
                result.nickname(),
                result.phoneNumber(),
                result.updatedAt()
        );
    }
}