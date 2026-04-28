package com.ojosama.userservice.application.dto.command;

import com.ojosama.userservice.domain.model.UserRole;
import java.util.UUID;

public record AdminChangeUserRoleCommand(
        UUID userId,
        UserRole role
) {
}
