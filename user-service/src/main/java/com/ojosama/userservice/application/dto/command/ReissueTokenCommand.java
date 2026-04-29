package com.ojosama.userservice.application.dto.command;

public record ReissueTokenCommand(
        String refreshToken
) {
}