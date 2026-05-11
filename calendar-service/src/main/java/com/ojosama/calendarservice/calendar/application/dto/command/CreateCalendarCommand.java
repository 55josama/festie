package com.ojosama.calendarservice.calendar.application.dto.command;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CreateCalendarCommand(
        String memo,
        UUID userId,
        UUID eventId,
        LocalDateTime eventDate
) {
}
