package com.ojosama.eventservice.event.presentation.dto.response;

import com.ojosama.eventservice.event.application.dto.result.EventResult;
import java.math.BigDecimal;
import java.util.UUID;

public record EventLocationResponse(
        UUID eventId,
        String eventName,
        String place,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer radius
) {
    public static EventLocationResponse from(EventResult result) {
        return new EventLocationResponse(
                result.id(),
                result.name(),
                result.place(),
                result.latitude(),
                result.longitude(),
                result.radius()
        );
    }
}
