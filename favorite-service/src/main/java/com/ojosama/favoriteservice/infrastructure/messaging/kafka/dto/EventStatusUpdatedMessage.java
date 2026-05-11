package com.ojosama.favoriteservice.infrastructure.messaging.kafka.dto;

import java.util.UUID;

public record EventStatusUpdatedMessage(
        UUID eventId,
        String status
) {
}
