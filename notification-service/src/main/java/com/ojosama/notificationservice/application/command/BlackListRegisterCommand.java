package com.ojosama.notificationservice.application.command;

import java.util.UUID;

public record BlackListRegisterCommand(
        UUID userId,
        String reason
) {
}
