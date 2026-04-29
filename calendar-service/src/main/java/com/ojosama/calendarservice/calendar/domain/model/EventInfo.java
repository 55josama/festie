package com.ojosama.calendarservice.calendar.domain.model;

import com.ojosama.calendarservice.calendar.domain.exception.CalendarErrorCode;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventInfo {

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "event_schedule_id", nullable = false)
    private UUID eventScheduleId;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "event_ticketing_date")
    private LocalDateTime eventTicketingDate;

    public EventInfo(UUID eventId, String eventName, UUID eventScheduleId, LocalDateTime eventDate,
                     LocalDateTime eventTicketingDate) {
        validateEventId(eventId);
        validateEventName(eventName);
        validateEventScheduleId(eventScheduleId);
        validateEventDate(eventDate);
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventScheduleId = eventScheduleId;
        this.eventDate = eventDate;
        this.eventTicketingDate = eventTicketingDate;
    }

    private void validateEventId(UUID eventId) {
        if (eventId == null) {
            throw new CalendarException(CalendarErrorCode.INVALID_INPUT);
        }
    }

    private void validateEventName(String eventName) {
        if (eventName == null || eventName.isBlank()) {
            throw new CalendarException(CalendarErrorCode.INVALID_INPUT);
        }
    }

    private void validateEventScheduleId(UUID eventScheduleId) {
        if (eventScheduleId == null) {
            throw new CalendarException(CalendarErrorCode.INVALID_INPUT);
        }
    }

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate == null) {
            throw new CalendarException(CalendarErrorCode.INVALID_INPUT);
        }
    }
}
