package com.ojosama.eventservice.event.domain.event.payload;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventStatusChangedMessage(
        UUID eventId,
        String eventName,
        String beforeStatus,
        String afterStatus,
        List<LocalDateTime> deletedScheduleIds
) {
}