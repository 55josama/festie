package com.ojosama.userservice.presentation.dto.response;

import com.ojosama.userservice.application.dto.result.AdminUserDetailResult;
import com.ojosama.userservice.domain.model.UserRole;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminDetailUserResponseDto(
        UUID userId,
        String email,
        String nickname,
        String name,
        String phoneNumber,
        UserRole role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminDetailUserResponseDto from(
            AdminUserDetailResult result
    ) {
        return new AdminDetailUserResponseDto(
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
