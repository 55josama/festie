package com.ojosama.calendarservice.calendar.domain.model;

import com.ojosama.calendarservice.calendar.domain.exception.CalendarErrorCode;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarException;
import lombok.Getter;

@Getter
public enum EventStatus {
    SCHEDULED("예정"),
    IN_PROGRESS("진행중"),
    COMPLETED("완료"),
    CANCELLED("취소");

    private final String value;

    EventStatus(String value) {
        this.value = value;
    }

    public static EventStatus from(String status) {
        try {
            return EventStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new CalendarException(CalendarErrorCode.INVALID_EVENT_STATUS);
        }
    }
}