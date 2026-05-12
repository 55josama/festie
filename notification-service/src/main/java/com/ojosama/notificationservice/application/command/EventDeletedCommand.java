package com.ojosama.notificationservice.application.command;

import java.util.List;
import java.util.UUID;

public record EventDeletedCommand(
        UUID eventId,
        String eventName,
        List<UUID> userIds
) {
}
