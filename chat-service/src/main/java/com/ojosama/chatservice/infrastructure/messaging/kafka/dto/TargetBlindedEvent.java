package com.ojosama.chatservice.infrastructure.messaging.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TargetBlindedEvent(
        UUID targetId,
        String targetType,
        UUID targetUserId,
        UUID categoryId,
        String category
) {
}

