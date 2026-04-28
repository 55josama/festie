package com.ojosama.calendarservice.calendar.application.dto.command;

import com.ojosama.calendarservice.calendar.presentaion.dto.CreateCalendarRequestDto;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CreateCalendarCommand(
        UUID eventScheduleId,
        String memo,
        UUID userId,
        UUID eventId
) {
    public static CreateCalendarCommand of(CreateCalendarRequestDto dto, UUID userId) {
        return CreateCalendarCommand.builder()
                .eventScheduleId(dto.eventScheduleId())
                .userId(userId)
                .memo(dto.memo())
                .eventId(dto.eventId())
                .build();
    }

}
