package com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.consumer.dto;

import java.util.UUID;

public record EventDeletedMessage(
        UUID eventId,
        String eventName
) {
}
