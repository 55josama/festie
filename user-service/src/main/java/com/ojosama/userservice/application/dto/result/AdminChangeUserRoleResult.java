package com.ojosama.userservice.application.dto.result;

import com.ojosama.userservice.domain.model.UserRole;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminChangeUserRoleResult(
        UUID userId,
        UserRole role,
        LocalDateTime updatedAt
) {
}
