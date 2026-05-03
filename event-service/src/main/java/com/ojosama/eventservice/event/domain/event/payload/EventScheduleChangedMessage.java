package com.ojosama.eventservice.event.domain.event.payload;

import com.ojosama.eventservice.event.domain.support.FieldChange;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventScheduleChangedMessage(
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
                .anyMatch(change -> change.fieldName().equals(fieldName));
    }
}
