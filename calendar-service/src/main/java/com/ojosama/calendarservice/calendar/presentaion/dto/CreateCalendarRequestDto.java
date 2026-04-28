package com.ojosama.calendarservice.calendar.presentaion.dto;

import com.ojosama.calendarservice.calendar.application.dto.command.CreateCalendarCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateCalendarRequestDto(
        @NotNull
        UUID eventScheduleId,
        @Size(max = 1000)
        String memo,
        @NotNull
        UUID eventId
) {
    public CreateCalendarCommand toCommand(UUID userId) {
        return CreateCalendarCommand.builder()
                .eventScheduleId(this.eventScheduleId)
                .memo(this.memo)
                .eventId(this.eventId)
                .userId(userId)
                .build();
    }
}
