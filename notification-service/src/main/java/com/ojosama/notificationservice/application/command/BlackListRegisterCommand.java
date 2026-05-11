package com.ojosama.notificationservice.application.command;

import java.util.UUID;

public record BlackListRegisterCommand(
        UUID targetUserId,
        String reason
) {
}
