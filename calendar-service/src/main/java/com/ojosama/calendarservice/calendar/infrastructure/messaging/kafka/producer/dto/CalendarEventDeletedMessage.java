package com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.producer.dto;

import java.util.List;
import java.util.UUID;

public record CalendarEventDeletedMessage(
        UUID eventId,
        String eventName,
        List<UUID> userIds
) {
}
