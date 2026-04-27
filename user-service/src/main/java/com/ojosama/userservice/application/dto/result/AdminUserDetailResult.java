package com.ojosama.userservice.application.dto.result;

import com.ojosama.userservice.domain.model.UserRole;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserDetailResult(
        UUID userId,
        String email,
        String nickname,
        String name,
        String phoneNumber,
        UserRole role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
