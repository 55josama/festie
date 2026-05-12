package com.ojosama.notificationservice.infrastructure.messaging.kafka.dto;

import java.util.List;
import java.util.UUID;

public record CalendarStatusChangeMessage(
        UUID eventId,
        String eventName,
        String status,
        List<UUID> userIds
) {
}
