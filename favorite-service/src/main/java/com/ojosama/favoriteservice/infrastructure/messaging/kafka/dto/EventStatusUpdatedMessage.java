package com.ojosama.favoriteservice.infrastructure.messaging.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventStatusUpdatedMessage(
        UUID eventId,
        String afterStatus
) {
}
