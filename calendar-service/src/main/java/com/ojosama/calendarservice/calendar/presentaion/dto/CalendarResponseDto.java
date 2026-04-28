package com.ojosama.calendarservice.calendar.presentaion.dto;

import com.ojosama.calendarservice.calendar.application.dto.result.CalendarResult;
import java.time.LocalDateTime;
import java.util.UUID;

public record CalendarResponseDto(
        UUID id,
        UUID eventScheduleId,
        LocalDateTime eventDate,
        LocalDateTime ticketingDate,
        String memo
) {
    public static CalendarResponseDto from(CalendarResult result) {
        return new CalendarResponseDto(
                result.id(),
                result.eventScheduleId(),
                result.eventDate(),
                result.ticketingDate(),
                result.memo());
    }
}
