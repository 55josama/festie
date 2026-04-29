package com.ojosama.eventservice.event.domain.event.payload;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventCreatedMessage(
        UUID eventId,
        String eventName,
        UUID categoryId,
        String categoryName,
        LocalDateTime eventStartAt,
        LocalDateTime eventEndAt
) {}
