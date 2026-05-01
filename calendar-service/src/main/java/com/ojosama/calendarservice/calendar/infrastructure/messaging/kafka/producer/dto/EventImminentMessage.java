package com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.producer.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventImminentMessage(
        UUID eventId,
        String eventName,
        LocalDateTime eventStartAt,
        List<UUID> userIds
) {
}
