package com.ojosama.chatservice.infrastructure.messaging.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventStatusChangedEvent(
        UUID eventId,
        String eventName,
        String beforeStatus,
        String afterStatus,
        List<UUID> deletedScheduleIds
) {
    public boolean isCancelled() {
        return "CANCELLED".equalsIgnoreCase(afterStatus);
    }
}
