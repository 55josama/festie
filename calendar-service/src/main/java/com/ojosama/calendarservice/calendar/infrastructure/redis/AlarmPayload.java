package com.ojosama.calendarservice.calendar.infrastructure.redis;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AlarmPayload(
        UUID eventId,
        String eventName,
        LocalDateTime alarmAt,
        List<UUID> userIds
) {
}
