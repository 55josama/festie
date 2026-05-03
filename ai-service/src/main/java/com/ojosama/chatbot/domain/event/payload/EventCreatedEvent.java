package com.ojosama.chatbot.domain.event.payload;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventCreatedEvent(
        UUID eventId,
        String eventName,
        UUID categoryId,
        String categoryName,
        LocalDateTime eventStartAt,
        LocalDateTime eventEndAt,
        String place,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer minFee,
        Integer maxFee,
        Boolean hasTicketing,
        LocalDateTime ticketingOpenAt,
        LocalDateTime ticketingCloseAt,
        String ticketingLink,
        String status,
        String officialLink,
        String description,
        String performer,
        String img
) { }
