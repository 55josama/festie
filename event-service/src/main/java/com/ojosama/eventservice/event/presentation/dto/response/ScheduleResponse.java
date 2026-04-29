package com.ojosama.eventservice.event.presentation.dto.response;

import com.ojosama.eventservice.event.application.dto.result.ScheduleResult;
import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleResponse(
    UUID id,
    String name,
    LocalDateTime startTime,
    LocalDateTime endTime
) {
    public static ScheduleResponse from(ScheduleResult result) {
        return new ScheduleResponse(
            result.id(),
            result.name(),
            result.startTime(),
            result.endTime()
        );
    }
}
