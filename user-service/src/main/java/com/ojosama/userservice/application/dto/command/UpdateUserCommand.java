package com.ojosama.userservice.application.dto.command;

import java.util.UUID;

public record UpdateUserCommand(
        UUID userId,
        String email,
        String nickname
) {
}
