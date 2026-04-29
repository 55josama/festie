package com.ojosama.eventservice.event.application.dto.result;

import com.ojosama.eventservice.event.domain.model.Event;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventResult(
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
    List<ScheduleResult> schedules
) {
    public static EventResult from(Event event) {
        return new EventResult(
            event.getId(),
            event.getName(),
            event.getCategory().getId(),
            event.getCategory().getName(),
            event.getEventTime().getStartAt(),
            event.getEventTime().getEndAt(),
            event.getEventLocation().getPlace(),
            event.getEventLocation().getLatitude(),
            event.getEventLocation().getLongitude(),
            event.getEventFee().getMinFee(),
            event.getEventFee().getMaxFee(),
            event.getEventTicketing().getHasTicketing(),
            event.getEventTicketing().getTicketingOpenAt(),
            event.getEventTicketing().getTicketingCloseAt(),
            event.getEventTicketing().getTicketingLink(),
            event.getOfficialLink(),
            event.getDescription(),
            event.getPerformer(),
            event.getImg(),
            event.getStatus().name(),
            event.getSchedules().stream().map(ScheduleResult::from).toList()
        );
    }
}
