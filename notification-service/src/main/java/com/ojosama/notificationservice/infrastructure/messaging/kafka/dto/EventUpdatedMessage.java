package com.ojosama.notificationservice.infrastructure.messaging.kafka.dto;

import java.util.List;
import java.util.UUID;

public record EventUpdatedMessage(
        UUID eventId,
        String eventName,
        List<ChangedField> changedFields,
        List<UUID> userIds
) {
    public record ChangedField(
            String fieldName,
            String before,
            String after
    ) {
    }
}
