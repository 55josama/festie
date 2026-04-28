package com.ojosama.notificationservice.infrastructure.messaging.kafka.consumer.dto;

import java.util.UUID;

public record TargetBlindEventMessage(
        UUID targetId,
        UUID targetUserId,
        String targetType,
        String categoryName
) {
}
