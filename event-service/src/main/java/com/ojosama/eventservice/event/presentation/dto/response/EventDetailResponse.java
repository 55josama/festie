package com.ojosama.eventservice.event.presentation.dto.response;

import com.ojosama.eventservice.event.application.dto.result.EventDetailResult;
import com.ojosama.eventservice.event.application.dto.result.EventResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventDetailResponse(
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
        List<ScheduleResponse> schedules,
        ChatRoomInfo chatRoom
) {
    public static EventDetailResponse from(EventDetailResult result) {
        EventResult event = result.event();
        return new EventDetailResponse(
                event.id(),
                event.name(),
                event.categoryId(),
                event.categoryName(),
                event.startAt(),
                event.endAt(),
                event.place(),
                event.latitude(),
                event.longitude(),
                event.minFee(),
                event.maxFee(),
                event.hasTicketing(),
                event.ticketingOpenAt(),
                event.ticketingCloseAt(),
                event.ticketingLink(),
                event.officialLink(),
                event.description(),
                event.performer(),
                event.img(),
                event.status(),
                event.schedules() != null
                        ? event.schedules().stream().map(ScheduleResponse::from).toList()
                        : List.of(),
                ChatRoomInfo.from(result.chatRoom())
        );
    }
}
