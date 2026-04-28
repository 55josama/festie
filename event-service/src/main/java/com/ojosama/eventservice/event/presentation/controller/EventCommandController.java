package com.ojosama.eventservice.event.presentation.controller;

import com.ojosama.common.exception.CommonErrorCode;
import com.ojosama.common.exception.CustomException;
import com.ojosama.common.response.ApiResponse;
import com.ojosama.eventservice.event.application.dto.command.CreateEventCommand;
import com.ojosama.eventservice.event.application.dto.command.UpdateEventCommand;
import com.ojosama.eventservice.event.application.dto.result.EventResult;
import com.ojosama.eventservice.event.application.service.EventCommandService;
import com.ojosama.eventservice.event.presentation.dto.request.CreateEventRequest;
import com.ojosama.eventservice.event.presentation.dto.request.UpdateEventRequest;
import com.ojosama.eventservice.event.presentation.dto.response.EventResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/events")
public class EventCommandController {

    private final EventCommandService eventCommandService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER')")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Valid @RequestBody CreateEventRequest request) {

        if (userId == null || userRole == null) {
            throw new CustomException(CommonErrorCode.INVALID_TOKEN);
        }

        EventResult result = eventCommandService.createEvent(CreateEventCommand.from(userId, request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(EventResponse.from(result)));
    }

    @PatchMapping("/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER')")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable UUID eventId,
            @Valid @RequestBody UpdateEventRequest request) {

        if (userId == null || userRole == null) {
            throw new CustomException(CommonErrorCode.INVALID_TOKEN);
        }

        EventResult result = eventCommandService.updateEvent(UpdateEventCommand.from(eventId, userId, request));
        return ResponseEntity.ok(ApiResponse.success(EventResponse.from(result)));
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER')")
    public ResponseEntity<Void> deleteEvent(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable UUID eventId) {

        if (userId == null || userRole == null) {
            throw new CustomException(CommonErrorCode.INVALID_TOKEN);
        }

        eventCommandService.deleteEvent(userId, eventId);
        return ResponseEntity.noContent().build();
    }
}
