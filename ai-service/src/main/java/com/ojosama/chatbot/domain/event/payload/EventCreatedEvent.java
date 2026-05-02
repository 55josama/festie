package com.ojosama.chatbot.domain.event.payload;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventCreatedEvent(
        UUID eventId,
        String eventName,
        UUID categoryId,
        String categoryName,
        LocalDateTime eventStartAt,
        LocalDateTime eventEndAt
) { }
