package com.ojosama.notificationservice.infrastructure.messaging.kafka.dto;

import java.util.List;
import java.util.UUID;

public record EventDeletedMessage(
        UUID eventId,
        String eventName,
        List<UUID> userIds
) {
}
