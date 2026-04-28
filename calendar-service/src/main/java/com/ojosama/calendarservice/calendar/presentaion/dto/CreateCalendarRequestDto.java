package com.ojosama.calendarservice.calendar.presentaion.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateCalendarRequestDto(
        @NotNull
        UUID eventScheduleId,
        String memo
) {
}
