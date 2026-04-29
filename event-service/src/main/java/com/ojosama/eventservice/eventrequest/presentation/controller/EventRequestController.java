package com.ojosama.eventservice.eventrequest.presentation.controller;

import com.ojosama.common.exception.CommonErrorCode;
import com.ojosama.common.exception.CustomException;
import com.ojosama.common.response.ApiResponse;
import com.ojosama.eventservice.eventrequest.application.dto.command.CreateEventRequestCommand;
import com.ojosama.eventservice.eventrequest.application.dto.command.EventRequestListCommand;
import com.ojosama.eventservice.eventrequest.application.dto.result.EventRequestResult;
import com.ojosama.eventservice.eventrequest.application.service.EventRequestCommandService;
import com.ojosama.eventservice.eventrequest.application.service.EventRequestQueryService;
import com.ojosama.eventservice.eventrequest.domain.model.EventRequestStatus;
import com.ojosama.eventservice.eventrequest.presentation.dto.request.CreateEventRequestRequest;
import com.ojosama.eventservice.eventrequest.presentation.dto.request.RejectEventRequestRequest;
import com.ojosama.eventservice.eventrequest.presentation.dto.response.EventRequestResponse;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/event-requests")
public class EventRequestController {

    private final EventRequestCommandService eventRequestCommandService;
    private final EventRequestQueryService eventRequestQueryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER')")
    public ResponseEntity<ApiResponse<EventRequestResponse>> createEventRequest(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Valid @RequestBody CreateEventRequestRequest request) {

        if (userId == null || userRole == null) {
            throw new CustomException(CommonErrorCode.INVALID_TOKEN);
        }

        EventRequestResult result = eventRequestCommandService.createEventRequest(
                CreateEventRequestCommand.from(userId, request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(EventRequestResponse.from(result)));
    }

    @DeleteMapping("/{requestId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER')")
    public ResponseEntity<Void> cancelEventRequest(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable UUID requestId) {

        if (userId == null || userRole == null) {
            throw new CustomException(CommonErrorCode.INVALID_TOKEN);
        }

        eventRequestCommandService.cancelEventRequest(userId, userRole, requestId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponse<EventRequestResponse>> getEventRequest(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable UUID requestId) {

        validateAuthHeaders(userId, userRole);

        EventRequestResult result = eventRequestQueryService.getEventRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success(EventRequestResponse.from(result)));
    }

    private void validateAuthHeaders(UUID userId, String userRole) {
        if (userId == null || !StringUtils.hasText(userRole)) {
            throw new CustomException(CommonErrorCode.INVALID_TOKEN);
        }
    }
}
