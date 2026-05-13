package com.ojosama.eventservice.event.presentation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.eventservice.event.application.dto.command.CreateEventCommand;
import com.ojosama.eventservice.event.application.dto.command.EventListCommand;
import com.ojosama.eventservice.event.application.dto.command.UpdateEventCommand;
import com.ojosama.eventservice.event.application.dto.result.EventListResult;
import com.ojosama.eventservice.event.application.dto.result.EventResult;
import com.ojosama.eventservice.event.application.service.EventCommandService;
import com.ojosama.eventservice.event.application.service.EventQueryService;
import com.ojosama.eventservice.event.domain.model.EventStatus;
import com.ojosama.eventservice.event.presentation.dto.request.CreateEventRequest;
import com.ojosama.eventservice.event.presentation.dto.request.UpdateEventRequest;
import com.ojosama.eventservice.event.presentation.dto.response.EventDetailResponse;
import com.ojosama.eventservice.event.presentation.dto.response.EventListResponse;
import com.ojosama.eventservice.event.presentation.dto.response.EventResponse;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/events")
public class EventController {

    private final EventCommandService eventCommandService;
    private final EventQueryService eventQueryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER')")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreateEventRequest request) {

        EventResult result = eventCommandService.createEvent(CreateEventCommand.from(UUID.fromString(userId), request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(EventResponse.from(result)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EventListResponse>>> getEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @PageableDefault(size = 10, sort = "eventTime.startAt", direction = Sort.Direction.ASC) Pageable pageable) {

        EventListCommand command = new EventListCommand(category, status, startAt, endAt, year, month);
        Page<EventListResult> result = eventQueryService.getEvents(command, pageable);
        return ResponseEntity.ok(ApiResponse.success(result.map(EventListResponse::from)));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventDetailResponse>> getEvent(
            @PathVariable UUID eventId) {

        EventResult result = eventQueryService.getEventById(eventId);
        return ResponseEntity.ok(ApiResponse.success(EventDetailResponse.from(result)));
    }

    @PatchMapping("/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER')")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID eventId,
            @Valid @RequestBody UpdateEventRequest request) {

        EventResult result = eventCommandService.updateEvent(UpdateEventCommand.from(eventId, UUID.fromString(userId), request));
        return ResponseEntity.ok(ApiResponse.success(EventResponse.from(result)));
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER')")
    public ResponseEntity<Void> deleteEvent(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID eventId) {

        eventCommandService.deleteEvent(UUID.fromString(userId), eventId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{eventId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER')")
    public ResponseEntity<ApiResponse<EventResponse>> cancelEvent(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID eventId) {

        EventResult result = eventCommandService.cancelEvent(eventId, UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success(EventResponse.from(result)));
    }
}
