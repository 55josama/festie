package com.ojosama.calendarservice.calendar.domain.event.payload;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventImminentMessage(
        UUID eventId,
        String eventName,
        LocalDateTime eventStartAt,
        List<UUID> userIds
) {
}
