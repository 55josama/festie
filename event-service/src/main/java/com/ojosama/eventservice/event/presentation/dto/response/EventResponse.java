package com.ojosama.eventservice.event.presentation.dto.response;

import com.ojosama.eventservice.event.application.dto.result.EventResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventResponse(
    UUID id,
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
    String img,
    String status,
    List<ScheduleResponse> schedules
) {
    public static EventResponse from(EventResult result) {
        return new EventResponse(
            result.id(),
            result.name(),
            result.categoryId(),
            result.categoryName(),
            result.startAt(),
            result.endAt(),
            result.place(),
            result.latitude(),
            result.longitude(),
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
            result.status(),
            result.schedules().stream().map(ScheduleResponse::from).toList()
        );
    }
}
