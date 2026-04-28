package com.ojosama.notificationservice.infrastructure.messaging.kafka.consumer.dto;

import java.util.UUID;

public record EventRequestCreatedMessage(
        UUID targetId,
        String categoryName,
        String eventName
) {
}
