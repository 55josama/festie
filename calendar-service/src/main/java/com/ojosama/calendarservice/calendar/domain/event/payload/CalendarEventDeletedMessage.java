package com.ojosama.calendarservice.calendar.domain.event.payload;

import java.util.List;
import java.util.UUID;

public record CalendarEventDeletedMessage(
        UUID eventId,
        String eventName,
        List<UUID> userIds
) {
}
