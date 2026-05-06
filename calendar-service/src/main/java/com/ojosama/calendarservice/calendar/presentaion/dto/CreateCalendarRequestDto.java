package com.ojosama.calendarservice.calendar.presentaion.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
        @JsonProperty("startTime")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        LocalDateTime eventStart
) {
    public CreateCalendarCommand toCommand(UUID userId) {
        return CreateCalendarCommand.builder()
                .memo(this.memo)
                .eventId(this.eventId)
                .userId(userId)
                .eventDate(this.eventStart)
                .build();
    }
}
