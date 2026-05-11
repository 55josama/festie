package com.ojosama.calendarservice.calendar.application.dto.command;

import com.ojosama.calendarservice.calendar.domain.model.EventStatus;
import java.util.UUID;

public record UpdateStatusEventCommand(
        UUID eventId,
        EventStatus status
) {
}
