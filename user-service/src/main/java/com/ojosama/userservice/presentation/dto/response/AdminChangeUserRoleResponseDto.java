package com.ojosama.userservice.presentation.dto.response;

import com.ojosama.userservice.application.dto.result.AdminChangeUserRoleResult;
import com.ojosama.userservice.domain.model.UserRole;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminChangeUserRoleResponseDto(
        UUID userId,
        UserRole role,
        LocalDateTime updatedAt
) {
    public static AdminChangeUserRoleResponseDto from(AdminChangeUserRoleResult result) {
        return new AdminChangeUserRoleResponseDto(
                result.userId(),
                result.role(),
                result.updatedAt()
        );
    }
}
