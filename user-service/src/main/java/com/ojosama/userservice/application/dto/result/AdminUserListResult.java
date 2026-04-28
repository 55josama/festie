package com.ojosama.userservice.application.dto.result;

import com.ojosama.userservice.domain.model.User;
import com.ojosama.userservice.domain.model.UserRole;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserListResult(
        UUID userId,
        String email,
        String nickname,
        String name,
        UserRole role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminUserListResult from(User user) {
        return new AdminUserListResult(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getName(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}