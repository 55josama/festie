package com.ojosama.favoriteservice.infrastructure.messaging.kafka.dto;

import java.util.List;
import java.util.UUID;

public record EventUpdatedMessage(
        UUID eventId,
        String eventName,
        List<FieldChange> changedFields
) {
    public record FieldChange(
            String fieldName,
            String before,
            String after
    ) {
    }
}
