package com.ojosama.calendarservice.calendar.domain.event.payload;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TicketingImminentMessage(
        UUID eventId,
        String eventName,
        LocalDateTime ticketingStartAt,
        List<UUID> userIds
) {
}
