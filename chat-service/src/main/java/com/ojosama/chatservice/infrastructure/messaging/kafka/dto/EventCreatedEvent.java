package com.ojosama.chatservice.infrastructure.messaging.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventCreatedEvent(
        UUID eventId,
        String eventName,
        UUID categoryId,
        String categoryCode,
        LocalDateTime eventStartAt,
        LocalDateTime eventEndAt
) {
}
