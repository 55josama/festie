package com.ojosama.eventservice.event.application.dto.command;

import com.ojosama.eventservice.event.domain.model.Event;
import com.ojosama.eventservice.event.domain.model.EventSchedule;
import com.ojosama.eventservice.event.domain.model.vo.ScheduleTime;
import java.time.LocalDateTime;

public record CreateScheduleCommand(
    String name,
    LocalDateTime startTime,
    LocalDateTime endTime
) {
    public EventSchedule toEntity(Event event) {
        return EventSchedule.builder()
            .event(event)
            .name(name)
            .scheduleTime(new ScheduleTime(startTime, endTime))
            .build();
    }
}
