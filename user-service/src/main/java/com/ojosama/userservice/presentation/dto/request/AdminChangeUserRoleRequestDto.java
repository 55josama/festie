package com.ojosama.userservice.presentation.dto.request;

import com.ojosama.userservice.application.dto.command.AdminChangeUserRoleCommand;
import com.ojosama.userservice.domain.model.UserRole;
import java.util.UUID;

public record AdminChangeUserRoleRequestDto(
        UserRole role
) {

    public AdminChangeUserRoleCommand toCommand(UUID userId) {
        return new AdminChangeUserRoleCommand(
                userId,
                role
        );
    }
}
