package com.ojosama.eventservice.event.presentation.controller;

import com.ojosama.common.exception.CommonErrorCode;
import com.ojosama.common.exception.CustomException;
import com.ojosama.common.response.ApiResponse;
import com.ojosama.eventservice.event.application.dto.command.CreateEventCommand;
import com.ojosama.eventservice.event.application.dto.command.CreateScheduleCommand;
import com.ojosama.eventservice.event.application.dto.result.EventResult;
import com.ojosama.eventservice.event.application.service.EventService;
import com.ojosama.eventservice.event.presentation.dto.request.CreateEventRequest;
import com.ojosama.eventservice.event.presentation.dto.response.EventResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/events")
public class EventController {

    private final EventService eventService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER')")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Valid @RequestBody CreateEventRequest request) {

        if (userId == null || userRole == null) {
            throw new CustomException(CommonErrorCode.INVALID_TOKEN);
        }

        List<CreateScheduleCommand> scheduleCommands = request.schedules().stream()
            .map(s -> new CreateScheduleCommand(s.name(), s.startTime(), s.endTime()))
            .toList();

        CreateEventCommand command = new CreateEventCommand(
            userId,
            request.name(),
            request.categoryId(),
            request.startAt(),
            request.endAt(),
            request.place(),
            request.latitude(),
            request.longitude(),
            request.minFee(),
            request.maxFee(),
            request.hasTicketing(),
            request.ticketingOpenAt(),
            request.ticketingCloseAt(),
            request.ticketingLink(),
            request.officialLink(),
            request.description(),
            request.performer(),
            request.img(),
            scheduleCommands
        );

        EventResult result = eventService.createEvent(command);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(EventResponse.from(result)));
    }
}
