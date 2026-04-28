package com.ojosama.eventservice.event.domain.event.payload;

import com.ojosama.eventservice.event.domain.support.FieldChange;
import java.util.List;
import java.util.UUID;

// Event의 필드 변경사항을 Kafka로 발행하는 메시지
public record EventScheduleChangedMessage(
        UUID eventId,
        String eventName,
        List<FieldChange> changedFields
) {
    public static EventScheduleChangedMessage from(
            UUID eventId,
            String eventName,
            List<FieldChange> changedFields
    ) {
        return new EventScheduleChangedMessage(eventId, eventName, changedFields);
    }

    // 변경된 필드가 있는지 확인

    public boolean hasChanges() {
        return changedFields != null && !changedFields.isEmpty();
    }

    // 특정 필드가 변경되었는지 확인

    public boolean isFieldChanged(String fieldName) {
        return changedFields != null && changedFields.stream()
                .anyMatch(change -> change.fieldName().equals(fieldName));
    }
}
