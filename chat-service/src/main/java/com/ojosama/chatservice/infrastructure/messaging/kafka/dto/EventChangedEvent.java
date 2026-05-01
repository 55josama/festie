package com.ojosama.chatservice.infrastructure.messaging.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventChangedEvent(
        UUID eventId,
        String eventName,
        List<FieldChange> changedFields
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FieldChange(
            String fieldName,
            Object before,
            Object after
    ) {
    }
}