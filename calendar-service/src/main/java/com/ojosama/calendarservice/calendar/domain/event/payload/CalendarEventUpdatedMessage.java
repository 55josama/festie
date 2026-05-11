package com.ojosama.calendarservice.calendar.domain.event.payload;

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
            Object before,
            Object after
    ) {
    }
}
