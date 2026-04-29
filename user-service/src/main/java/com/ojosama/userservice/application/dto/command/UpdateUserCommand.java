package com.ojosama.userservice.application.dto.command;

import java.util.UUID;

public record UpdateUserCommand(
        UUID userId,
        String name,
        String nickname,
        String phoneNumber
) {
}