package com.ojosama.calendarservice.calendar.presentaion.dto;

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
}
