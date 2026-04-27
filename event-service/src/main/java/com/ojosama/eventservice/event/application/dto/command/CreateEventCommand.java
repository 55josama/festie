package com.ojosama.eventservice.event.application.dto.command;

import com.ojosama.eventservice.event.domain.model.Event;
import com.ojosama.eventservice.event.domain.model.EventCategory;
import com.ojosama.eventservice.event.domain.model.EventStatus;
import com.ojosama.eventservice.event.domain.model.vo.EventFee;
import com.ojosama.eventservice.event.domain.model.vo.EventLocation;
import com.ojosama.eventservice.event.domain.model.vo.EventTicketing;
import com.ojosama.eventservice.event.domain.model.vo.EventTime;
import com.ojosama.eventservice.event.presentation.dto.request.CreateEventRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreateEventCommand(
    UUID userId,
    String name,
    UUID categoryId,
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
    List<CreateScheduleCommand> schedules
) {
    public static CreateEventCommand from(UUID userId, CreateEventRequest request) {
        List<CreateScheduleCommand> scheduleCommands = request.schedules().stream()
                .map(s -> new CreateScheduleCommand(s.name(), s.startTime(), s.endTime()))
                .toList();

        return new CreateEventCommand(
                userId,
                request.name(), request.categoryId(),
                request.startAt(), request.endAt(),
                request.place(), request.latitude(), request.longitude(),
                request.minFee(), request.maxFee(),
                request.hasTicketing(),
                request.ticketingOpenAt(), request.ticketingCloseAt(), request.ticketingLink(),
                request.officialLink(),
                request.description(), request.performer(), request.img(),
                scheduleCommands
        );
    }

    public Event toEntity(EventCategory category) {
        return Event.builder()
            .name(name)
            .category(category)
            .eventTime(new EventTime(startAt, endAt))
            .eventLocation(new EventLocation(place, latitude, longitude))
            .eventFee(new EventFee(minFee, maxFee))
            .eventTicketing(new EventTicketing(hasTicketing, ticketingOpenAt, ticketingCloseAt, ticketingLink))
            .officialLink(officialLink)
            .description(description)
            .performer(performer)
            .img(img)
            .status(EventStatus.SCHEDULED)
            .build();
    }
}
