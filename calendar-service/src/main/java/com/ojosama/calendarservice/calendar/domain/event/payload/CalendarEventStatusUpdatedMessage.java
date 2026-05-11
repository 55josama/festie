package com.ojosama.calendarservice.calendar.domain.event.payload;

import java.util.List;
import java.util.UUID;

public record CalendarEventStatusUpdatedMessage(
        UUID eventId,
        String status,
        List<UUID> userIds
) {
}
