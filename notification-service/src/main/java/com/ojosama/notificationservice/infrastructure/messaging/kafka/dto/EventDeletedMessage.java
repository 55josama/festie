package com.ojosama.notificationservice.infrastructure.messaging.kafka.dto;

import java.util.UUID;

public record EventDeletedMessage(
        UUID eventId,
        String eventName
) {
}
