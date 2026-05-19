package com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.consumer.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventStatusUpdatedMessage(
        UUID eventId,
        String eventName,
        String afterStatus,
        List<LocalDateTime> deletedScheduleIds
) {
}