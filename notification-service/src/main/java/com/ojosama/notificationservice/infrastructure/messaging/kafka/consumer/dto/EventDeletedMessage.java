package com.ojosama.notificationservice.infrastructure.messaging.kafka.consumer.dto;

import java.util.UUID;

public record EventDeletedMessage(
        UUID eventId,
        String eventName
) {
}
