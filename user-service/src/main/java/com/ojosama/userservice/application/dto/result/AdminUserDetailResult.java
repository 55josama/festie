package com.ojosama.userservice.application.dto.result;

import com.ojosama.userservice.domain.model.UserRole;
import com.ojosama.userservice.domain.model.UserStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserDetailResult(
        UUID userId,
        String email,
        String nickname,
        String name,
        String phoneNumber,
        UserRole role,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
