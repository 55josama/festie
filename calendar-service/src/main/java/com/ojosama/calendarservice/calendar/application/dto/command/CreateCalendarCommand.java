package com.ojosama.calendarservice.calendar.application.dto.command;

import java.util.UUID;
import lombok.Builder;

@Builder
public record CreateCalendarCommand(
        UUID eventScheduleId,
        String memo,
        UUID userId,
        UUID eventId
) {
}
