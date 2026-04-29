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
        String memo,
        String eventName,
        UUID eventId
) {
    public static CalendarResult from(Calendar calendar) {
        return CalendarResult.builder()
                .id(calendar.getId())
                .eventScheduleId(calendar.getEventInfo().getEventScheduleId())
                .eventDate(calendar.getEventInfo().getEventDate())
                .ticketingDate(calendar.getEventInfo().getEventTicketingDate())
                .memo(calendar.getMemo())
                .eventName(calendar.getEventInfo().getEventName())
                .eventId(calendar.getEventInfo().getEventId())
                .build();
    }
}
