package com.ojosama.notificationservice.infrastructure.messaging.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TargetBlindEventMessage(
        UUID targetId,
        UUID targetUserId,
        String targetType,
        String category
) {
}
