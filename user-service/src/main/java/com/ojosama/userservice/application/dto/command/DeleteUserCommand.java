package com.ojosama.userservice.application.dto.command;

import java.util.UUID;

public record DeleteUserCommand(
        UUID userId
) {
}
