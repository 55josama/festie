package com.ojosama.notificationservice.infrastructure.messaging.kafka.dto;

import java.util.UUID;

public record EventRequestMessage(
        UUID targetId,
        String categoryName,
        String eventName
) {
}
