package com.ojosama.notificationservice.infrastructure.messaging.kafka.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TicketingScheduleMessage(
        List<UUID> userIds,
        UUID eventId,
        String eventName,
        LocalDateTime ticketingStartAt
) {
}
