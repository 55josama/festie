package com.ojosama.calendarservice.calendar.domain.model;

public record FieldChange(
        String fieldName,
        String before,
        String after
) {
}
