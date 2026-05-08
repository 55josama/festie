package com.ojosama.chatservice.infrastructure.messaging.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TargetUnblindedEvent(
        UUID targetId,
        String targetType,
        String reason
) {
}
