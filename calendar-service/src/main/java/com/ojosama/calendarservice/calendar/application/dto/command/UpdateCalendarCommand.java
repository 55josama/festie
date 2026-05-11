package com.ojosama.calendarservice.calendar.application.dto.command;

import java.util.UUID;
import lombok.Builder;

@Builder
public record UpdateCalendarCommand(
        String memo,
        UUID userId,
        UUID calendarId
) {
}
