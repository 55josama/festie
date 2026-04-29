package com.ojosama.chatbot.domain.event.payload;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventsUpdateEvent(
        UUID eventId,
        String name,
        String categoryName,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String place,
        Boolean hasTicketing,
        String officialLink,
        String description,
        String performer,
        String status
) { }
