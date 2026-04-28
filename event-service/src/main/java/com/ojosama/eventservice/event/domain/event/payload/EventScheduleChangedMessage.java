package com.ojosama.eventservice.event.domain.event.payload;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventScheduleChangedMessage(
        UUID eventId,
        String eventName,
        LocalDateTime newStartAt,
        LocalDateTime newEndAt
) {}
