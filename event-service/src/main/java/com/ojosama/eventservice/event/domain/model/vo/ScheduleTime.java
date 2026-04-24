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
public class ScheduleTime {
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public ScheduleTime(LocalDateTime startTime, LocalDateTime endTime) {
        validateScheduleTime(startTime, endTime);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    private void validateScheduleTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new EventException(EventErrorCode.EVENT_SCHEDULE_INVALID_TIME);
        }
        if (startTime.isAfter(endTime)) {
            throw new EventException(EventErrorCode.EVENT_SCHEDULE_INVALID_TIME);
        }
        if (startTime.equals(endTime)) {
            throw new EventException(EventErrorCode.EVENT_SCHEDULE_INVALID_TIME);
        }
    }

    public boolean isScheduleStarted() {
        return LocalDateTime.now().isAfter(startTime);
    }

    public boolean isScheduleEnded() {
        return LocalDateTime.now().isAfter(endTime);
    }

    public boolean isScheduleInProgress() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startTime) && now.isBefore(endTime);
    }

    public long getDurationMinutes() {
        return java.time.temporal.ChronoUnit.MINUTES.between(startTime, endTime);
    }
}