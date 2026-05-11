package com.ojosama.notificationservice.application.command;

import java.util.List;
import java.util.UUID;

public record CalendarStatusChangeCommand(
        UUID eventId,
        String eventName,
        String status,
        List<UUID> userIds
) {
}
