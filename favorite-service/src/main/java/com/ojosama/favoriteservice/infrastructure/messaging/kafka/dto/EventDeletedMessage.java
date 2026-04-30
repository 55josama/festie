package com.ojosama.favoriteservice.infrastructure.messaging.kafka.dto;

import java.util.UUID;

public record EventDeletedMessage(
        UUID eventId,
        String eventName
) {
}
