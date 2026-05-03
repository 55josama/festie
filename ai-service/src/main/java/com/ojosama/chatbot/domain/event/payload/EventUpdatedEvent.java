package com.ojosama.chatbot.domain.event.payload;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record EventUpdatedEvent(
        UUID eventId,
        String eventName,
        List<FieldChange> changedFields,
        String place,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer minFee,
        Integer maxFee,
        Boolean hasTicketing,
        LocalDateTime ticketingOpenAt,
        LocalDateTime ticketingCloseAt,
        String ticketingLink,
        String status,
        String officialLink,
        String description,
        String performer,
        String img
) {
    public boolean hasChanges() {
        return changedFields != null && !changedFields.isEmpty();
    }

    public boolean isFieldChanged(String fieldName) {
        return changedFields != null && changedFields.stream()
                .filter(Objects::nonNull)
                .anyMatch(change -> Objects.equals(change.fieldName(), fieldName));
    }

    public record FieldChange(
            String fieldName,
            Object before,
            Object after
    ) { }
}
