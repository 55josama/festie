package com.ojosama.calendarservice.calendar.presentation.dto.request;

import com.ojosama.calendarservice.calendar.application.dto.command.CreateCalendarCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateCalendarRequestDto(
        @Size(max = 1000)
        String memo,
        @NotNull
        UUID eventId,
        @NotNull
        LocalDateTime eventDate
) {
    public CreateCalendarCommand toCommand(UUID userId) {
        return CreateCalendarCommand.builder()
                .eventDate(this.eventDate)
                .memo(this.memo)
                .eventId(this.eventId)
                .userId(userId)
                .build();
    }
}
