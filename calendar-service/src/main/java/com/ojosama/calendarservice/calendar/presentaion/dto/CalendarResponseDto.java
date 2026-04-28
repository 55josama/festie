package com.ojosama.calendarservice.calendar.presentaion.dto;

import com.ojosama.calendarservice.calendar.application.dto.result.CalendarResult;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CalendarResponseDto(
        UUID id,
        UUID eventScheduleId,
        LocalDateTime eventDate,
        LocalDateTime ticketingDate,
        String memo,
        String eventName,
        UUID eventId
) {
    public static CalendarResponseDto from(CalendarResult result) {
        return CalendarResponseDto.builder()
                .id(result.id())
                .eventScheduleId(result.eventScheduleId())
                .eventDate(result.eventDate())
                .ticketingDate(result.ticketingDate())
                .memo(result.memo())
                .eventName(result.eventName())
                .eventId(result.eventId())
                .build();
    }
}
