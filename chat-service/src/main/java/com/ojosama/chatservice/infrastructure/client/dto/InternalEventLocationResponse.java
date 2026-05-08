package com.ojosama.chatservice.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InternalEventLocationResponse(
        UUID eventId,
        String eventName,
        String place,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer radiusMeters
) {
}
