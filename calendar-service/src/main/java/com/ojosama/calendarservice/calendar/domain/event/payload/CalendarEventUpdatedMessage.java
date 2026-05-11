package com.ojosama.calendarservice.calendar.domain.event.payload;

import com.ojosama.calendarservice.calendar.domain.model.FieldChange;
import java.util.List;
import java.util.UUID;

public record CalendarEventUpdatedMessage(
        UUID eventId,
        String eventName,
        List<UUID> userIds,
        List<FieldChange> changedFields
) {
}
