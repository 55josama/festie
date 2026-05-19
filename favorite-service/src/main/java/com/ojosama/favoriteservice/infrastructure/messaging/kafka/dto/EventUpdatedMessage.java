package com.ojosama.favoriteservice.infrastructure.messaging.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventUpdatedMessage(
        UUID eventId,
        String eventName,
        List<FieldChange> changedFields,
        String status,
        String img
) {
    public record FieldChange(
            String fieldName,
            String before,
            String after
    ) {
    }
}
