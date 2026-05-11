package com.ojosama.calendarservice.calendar.application.dto.command;

import java.util.UUID;

public record DeleteCalendarCommand(
        UUID calendarId,
        UUID userId
) {
}
