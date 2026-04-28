package com.ojosama.notificationservice.infrastructure.messaging.kafka.consumer.dto;

import java.util.List;
import java.util.UUID;

public record EventChangedMessage(
        UUID eventId,
        String eventName,
        List<ChangedField> changedFields
) {
    public record ChangedField(
            String fieldName,
            String before,
            String after
    ) {
    }
}
