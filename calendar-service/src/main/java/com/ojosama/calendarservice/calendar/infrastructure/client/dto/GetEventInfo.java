package com.ojosama.calendarservice.calendar.infrastructure.client.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GetEventInfo(
        UUID id,
        String name,
        LocalDateTime ticketingOpenAt,
        List<ScheduleResponse> schedules
) {
    public record ScheduleResponse(
            UUID id,
            LocalDateTime startTime
    ) {
    }
}