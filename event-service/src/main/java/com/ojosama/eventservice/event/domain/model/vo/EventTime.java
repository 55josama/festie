package com.ojosama.eventservice.event.domain.model.vo;

import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventTime {

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    public EventTime(LocalDateTime startAt, LocalDateTime endAt) {
        validateEventTime(startAt, endAt);
        this.startAt = startAt;
        this.endAt = endAt;
    }

    private void validateEventTime(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null) {
            throw new EventException(EventErrorCode.EVENT_INVALID_TIME);
        }
        if (startAt.isAfter(endAt)) {
            throw new EventException(EventErrorCode.EVENT_INVALID_TIME);
        }
        if (startAt.equals(endAt)) {
            throw new EventException(EventErrorCode.EVENT_INVALID_TIME);
        }
        if (startAt.isBefore(LocalDateTime.now())) {
            throw new EventException(EventErrorCode.EVENT_PAST_START_TIME);
        }
    }

    public boolean isEventStarted() {
        return LocalDateTime.now().isAfter(startAt);
    }

    public boolean isEventEnded() {
        return LocalDateTime.now().isAfter(endAt);
    }

    public boolean isEventInProgress() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startAt) && now.isBefore(endAt);
    }
}
