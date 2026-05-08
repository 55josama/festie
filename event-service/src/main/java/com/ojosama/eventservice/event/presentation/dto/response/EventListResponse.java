package com.ojosama.eventservice.event.presentation.dto.response;

import com.ojosama.eventservice.event.application.dto.result.EventListResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventListResponse(
        UUID id,
        String name,
        UUID categoryId,
        String categoryName,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String place,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer radius,
        Integer minFee,
        Integer maxFee,
        Boolean hasTicketing,
        LocalDateTime ticketingOpenAt,
        LocalDateTime ticketingCloseAt,
        String ticketingLink,
        String officialLink,
        String description,
        String performer,
        String img,
        String status
) {
    public static EventListResponse from(EventListResult result) {
        return new EventListResponse(
                result.id(),
                result.name(),
                result.categoryId(),
                result.categoryName(),
                result.startAt(),
                result.endAt(),
                result.place(),
                result.latitude(),
                result.longitude(),
                result.radius(),
                result.minFee(),
                result.maxFee(),
                result.hasTicketing(),
                result.ticketingOpenAt(),
                result.ticketingCloseAt(),
                result.ticketingLink(),
                result.officialLink(),
                result.description(),
                result.performer(),
                result.img(),
                result.status()
        );
    }
}
