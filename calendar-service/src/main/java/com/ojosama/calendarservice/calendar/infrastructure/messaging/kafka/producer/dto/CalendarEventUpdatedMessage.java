package com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.producer.dto;

import java.util.List;
import java.util.UUID;

public record CalendarEventUpdatedMessage(
        UUID eventId,
        String eventName,
        List<UUID> userIds,
        List<FieldChange> changedFields
) {
    public record FieldChange(
            String fieldName,
            String before,
            String after
    ) {
    }
}
