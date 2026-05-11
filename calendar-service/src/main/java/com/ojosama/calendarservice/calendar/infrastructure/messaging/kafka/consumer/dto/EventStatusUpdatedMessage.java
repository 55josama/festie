package com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.consumer.dto;

import java.util.UUID;

public record EventStatusUpdatedMessage(
        UUID eventId,
        String eventName,
        String status
) {
}