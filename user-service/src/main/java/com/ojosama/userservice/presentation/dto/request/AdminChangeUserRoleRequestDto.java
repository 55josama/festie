package com.ojosama.userservice.presentation.dto.request;

import com.ojosama.userservice.application.dto.command.AdminChangeUserRoleCommand;
import com.ojosama.userservice.domain.model.UserRole;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AdminChangeUserRoleRequestDto(
        @NotNull(message = "역할은 필수입니다.")
        UserRole role
) {

    public AdminChangeUserRoleCommand toCommand(UUID userId) {
        return new AdminChangeUserRoleCommand(
                userId,
                role
        );
    }
}