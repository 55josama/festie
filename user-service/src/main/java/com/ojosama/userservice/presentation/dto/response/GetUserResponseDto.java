package com.ojosama.userservice.presentation.dto.response;

import com.ojosama.userservice.application.dto.result.GetUserResult;
import com.ojosama.userservice.domain.model.UserRole;
import java.time.LocalDateTime;
import java.util.UUID;

public record GetUserResponseDto(
        UUID userId,
        String email,
        String nickname,
        String name,
        String phoneNumber,
        UserRole role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static GetUserResponseDto from(GetUserResult result) {
        return new GetUserResponseDto(
                result.userId(),
                result.email(),
                result.nickname(),
                result.name(),
                result.phoneNumber(),
                result.role(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
