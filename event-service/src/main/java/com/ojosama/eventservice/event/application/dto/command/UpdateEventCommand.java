package com.ojosama.eventservice.event.application.dto.command;

import com.ojosama.eventservice.event.presentation.dto.request.UpdateEventRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UpdateEventCommand(
        UUID eventId,
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
    public static UpdateEventCommand from(UUID eventId, UUID userId, UpdateEventRequest request) {
        List<CreateScheduleCommand> scheduleCommands = request.schedules() == null ? null
                : request.schedules().stream()
                        .map(s -> new CreateScheduleCommand(s.name(), s.startTime(), s.endTime()))
                        .toList();

        return new UpdateEventCommand(
                eventId, userId,
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
}

