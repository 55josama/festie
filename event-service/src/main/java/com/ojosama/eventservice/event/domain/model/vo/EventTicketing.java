package com.ojosama.eventservice.event.domain.model.vo;

import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventTicketing {
    private Boolean hasTicketing;
    private LocalDateTime ticketingOpenAt;
    private LocalDateTime ticketingCloseAt;
    private String ticketingLink;

    public EventTicketing(Boolean hasTicketing, LocalDateTime ticketingOpenAt,
                          LocalDateTime ticketingCloseAt, String ticketingLink) {
        validateTicketing(hasTicketing, ticketingOpenAt, ticketingCloseAt, ticketingLink);
        this.hasTicketing = hasTicketing;
        this.ticketingOpenAt = ticketingOpenAt;
        this.ticketingCloseAt = ticketingCloseAt;
        this.ticketingLink = ticketingLink;
    }

    private void validateTicketing(Boolean hasTicketing, LocalDateTime ticketingOpenAt,
                                   LocalDateTime ticketingCloseAt, String ticketingLink) {
        if (hasTicketing == null) {
            throw new EventException(EventErrorCode.TICKETING_INVALID_TIME);
        }

        if (hasTicketing) {
            if (ticketingOpenAt == null || ticketingCloseAt == null) {
                throw new EventException(EventErrorCode.TICKETING_INVALID_TIME);
            }
            if (ticketingOpenAt.isAfter(ticketingCloseAt)) {
                throw new EventException(EventErrorCode.TICKETING_INVALID_TIME);
            }
            if (ticketingOpenAt.equals(ticketingCloseAt)) {
                throw new EventException(EventErrorCode.TICKETING_INVALID_TIME);
            }
            if (ticketingLink == null || ticketingLink.isBlank()) {
                throw new EventException(EventErrorCode.TICKETING_NOT_AVAILABLE);
            }
            if (ticketingLink.length() > 500) {
                throw new EventException(EventErrorCode.TICKETING_NOT_AVAILABLE);
            }
        }
    }

    public boolean isTicketingOpen() {
        if (!hasTicketing) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(ticketingOpenAt) && now.isBefore(ticketingCloseAt);
    }

    public boolean isTicketingClosed() {
        if (!hasTicketing) {
            return true;
        }
        return LocalDateTime.now().isAfter(ticketingCloseAt);
    }

    public void validateTicketingAvailable() {
        if (!isTicketingOpen()) {
            if (LocalDateTime.now().isBefore(ticketingOpenAt)) {
                throw new EventException(EventErrorCode.TICKETING_NOT_OPENED);
            }
            throw new EventException(EventErrorCode.TICKETING_CLOSED);
        }
    }
}