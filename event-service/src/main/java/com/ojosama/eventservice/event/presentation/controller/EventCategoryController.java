package com.ojosama.eventservice.event.presentation.controller;

import com.ojosama.common.exception.CommonErrorCode;
import com.ojosama.common.exception.CustomException;
import com.ojosama.common.response.ApiResponse;
import com.ojosama.eventservice.event.application.dto.command.CreateEventCategoryCommand;
import com.ojosama.eventservice.event.application.dto.command.UpdateEventCategoryCommand;
import com.ojosama.eventservice.event.application.dto.result.EventCategoryResult;
import com.ojosama.eventservice.event.application.service.EventCategoryCommandService;
import com.ojosama.eventservice.event.presentation.dto.request.CreateEventCategoryRequest;
import com.ojosama.eventservice.event.presentation.dto.request.UpdateEventCategoryRequest;
import com.ojosama.eventservice.event.presentation.dto.response.EventCategoryResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/event-categories")
public class EventCategoryController {

    private final EventCategoryCommandService eventCategoryCommandService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventCategoryResponse>> createCategory(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Valid @RequestBody CreateEventCategoryRequest request) {

        if (userId == null || userRole == null) {
            throw new CustomException(CommonErrorCode.INVALID_TOKEN);
        }

        CreateEventCategoryCommand command = new CreateEventCategoryCommand(userId, request.name());
        EventCategoryResult result = eventCategoryCommandService.createCategory(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(EventCategoryResponse.from(result)));
    }

    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventCategoryResponse>> updateCategory(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateEventCategoryRequest request) {

        if (userId == null || userRole == null) {
            throw new CustomException(CommonErrorCode.INVALID_TOKEN);
        }

        UpdateEventCategoryCommand command = new UpdateEventCategoryCommand(userId, categoryId, request.name());
        EventCategoryResult result = eventCategoryCommandService.updateCategory(command);
        return ResponseEntity.ok(ApiResponse.success(EventCategoryResponse.from(result)));
    }
}
