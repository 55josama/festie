package com.ojosama.eventservice.event.presentation.controller;

import com.ojosama.common.exception.CommonErrorCode;
import com.ojosama.common.exception.CustomException;
import com.ojosama.common.response.ApiResponse;
import com.ojosama.eventservice.event.application.dto.command.EventListCommand;
import com.ojosama.eventservice.event.application.dto.result.EventResult;
import com.ojosama.eventservice.event.application.service.EventQueryService;
import com.ojosama.eventservice.event.domain.model.EventStatus;
import com.ojosama.eventservice.event.presentation.dto.response.EventResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/events")
public class EventQueryController {

    private final EventQueryService eventQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EventResponse>>> getEvents(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @PageableDefault(size = 10, sort = "eventTime.startAt", direction = Sort.Direction.ASC) Pageable pageable) {

        validateAuthHeaders(userId, userRole);

        EventListCommand command = new EventListCommand(category, status, startAt, endAt, year, month);
        Page<EventResult> result = eventQueryService.getEvents(command, pageable);
        return ResponseEntity.ok(ApiResponse.success(result.map(EventResponse::from)));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable UUID eventId) {

        validateAuthHeaders(userId, userRole);

        EventResult result = eventQueryService.getEventById(eventId);
        return ResponseEntity.ok(ApiResponse.success(EventResponse.from(result)));
    }

    private void validateAuthHeaders(UUID userId, String userRole) {
        if (userId == null || !StringUtils.hasText(userRole)) {
            throw new CustomException(CommonErrorCode.INVALID_TOKEN);
        }
    }
}
