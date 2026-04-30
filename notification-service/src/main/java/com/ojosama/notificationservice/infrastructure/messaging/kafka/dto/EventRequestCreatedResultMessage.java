package com.ojosama.notificationservice.infrastructure.messaging.kafka.dto;

import java.util.UUID;

public record EventRequestCreatedResultMessage(
        UUID targetId,
        UUID receiverId,
        String status,
        String eventName
) {
}
