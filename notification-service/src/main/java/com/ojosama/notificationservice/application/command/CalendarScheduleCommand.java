package com.ojosama.notificationservice.application.command;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CalendarScheduleCommand(
        List<UUID> userIds,
        UUID eventId,
        String eventName,
        LocalDateTime eventStartAt
) {
}
