package com.ojosama.chatbot.domain.event.payload;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventsUpdateEvent(
        UUID eventId,
        String name,
        UUID categoryId,
        String categoryName,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String place,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer minFee,
        Integer maxFee,
        Boolean hasTicketing,
        LocalDateTime ticketingOpenAt,
        LocalDateTime ticketingCloseAt,
        String ticketingLink,
        String officialLink,
        String description,
        String performer,
        String status
) { }
