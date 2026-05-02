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

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "event_ticketing_date")
    private LocalDateTime eventTicketingDate;

    public EventInfo(UUID eventId, String eventName, LocalDateTime eventDate,
                     LocalDateTime eventTicketingDate) {
        validateEventId(eventId);
        validateEventName(eventName);
        validateEventDate(eventDate);
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventTicketingDate = eventTicketingDate;
    }

    public void updateEventDate(LocalDateTime eventDate) {
        validateEventDate(eventDate);
        this.eventDate = eventDate;
    }

    public void updateTicketingDate(LocalDateTime eventTicketingDate) {
        this.eventTicketingDate = eventTicketingDate;
    }

    public void updateEventName(String eventName) {
        validateEventName(eventName);
        this.eventName = eventName;
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

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate == null) {
            throw new CalendarException(CalendarErrorCode.INVALID_INPUT);
        }
    }

}
