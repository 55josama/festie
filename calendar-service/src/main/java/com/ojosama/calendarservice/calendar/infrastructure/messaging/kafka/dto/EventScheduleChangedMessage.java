package com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.dto;

import java.util.List;
import java.util.UUID;

public record EventScheduleChangedMessage(
        UUID eventId,
        String eventName,
        List<FieldChange> changedFields
) {}
