package com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.dto;

public record FieldChange(
        String fieldName,
        Object before,
        Object after
) {}
