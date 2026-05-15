package com.ojosama.eventservice.event.application.dto.result;

import com.ojosama.eventservice.event.domain.model.EventSchedule;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleResult(
    UUID id,
    String name,
    LocalDateTime startTime,
    LocalDateTime endTime
) implements Serializable {
    public static ScheduleResult from(EventSchedule schedule) {
        return new ScheduleResult(
            schedule.getId(),
            schedule.getName(),
            schedule.getScheduleTime().getStartTime(),
            schedule.getScheduleTime().getEndTime()
        );
    }
}
