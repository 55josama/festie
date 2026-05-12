package com.ojosama.notificationservice.application.command;

import java.util.UUID;

public record EventRequestResultCommand(
        UUID targetId,
        UUID receiverId,
        String status,
        String eventName
) {
}
