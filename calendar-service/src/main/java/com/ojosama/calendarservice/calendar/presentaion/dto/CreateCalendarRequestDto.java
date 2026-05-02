package com.ojosama.calendarservice.calendar.presentaion.dto;

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
        LocalDateTime eventDate,
        LocalDateTime ticketingDate,
        @NotNull
        String eventName
) {
    public CreateCalendarCommand toCommand(UUID userId) {
        return CreateCalendarCommand.builder()
                .eventDate(this.eventDate)
                .ticketingDate(this.ticketingDate)
                .memo(this.memo)
                .eventId(this.eventId)
                .userId(userId)
                .eventName(this.eventName)
                .build();
    }
}
