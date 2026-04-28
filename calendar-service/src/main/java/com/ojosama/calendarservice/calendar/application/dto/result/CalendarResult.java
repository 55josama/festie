package com.ojosama.calendarservice.calendar.application.dto.result;

import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CalendarResult(
        UUID id,
        UUID eventScheduleId,
        LocalDateTime eventDate,
        LocalDateTime ticketingDate,
        String memo
) {
    public static CalendarResult from(Calendar calendar) {
        return CalendarResult.builder()
                .id(calendar.getId())
                .eventScheduleId(calendar.getEventScheduleId())
                .eventDate(calendar.getEventDate())
                .ticketingDate(calendar.getEventTicketingDate())
                .memo(calendar.getMemo())
                .build();
    }
}
