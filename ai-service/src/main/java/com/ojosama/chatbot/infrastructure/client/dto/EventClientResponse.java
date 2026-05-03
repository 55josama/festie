package com.ojosama.chatbot.infrastructure.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventClientResponse(
        UUID id,
        String name,
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
        String status,
        String img
) { }
