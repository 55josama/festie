package com.ojosama.calendarservice.calendar.application.dto.query;

import java.util.UUID;

public record ListCalendarQuery(
        UUID userId,
        int year,
        int month
) {
    public static ListCalendarQuery of(UUID userId, int year, int month) {
        return new ListCalendarQuery(userId, year, month);
    }
}
