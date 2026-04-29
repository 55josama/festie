package com.ojosama.userservice.presentation.dto.response;

import com.ojosama.userservice.application.dto.result.AdminUserListResult;
import com.ojosama.userservice.domain.model.UserRole;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserListResponseDto(
        UUID userId,
        String email,
        String nickname,
        String name,
        UserRole role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AdminUserListResponseDto from(AdminUserListResult result) {
        return new AdminUserListResponseDto(
                result.userId(),
                result.email(),
                result.nickname(),
                result.name(),
                result.role(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
