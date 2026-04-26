package com.ojosama.eventservice.event.presentation.controller;

import com.ojosama.common.exception.CommonErrorCode;
import com.ojosama.common.exception.CustomException;
import com.ojosama.common.response.ApiResponse;
import com.ojosama.eventservice.event.application.dto.command.CreateEventCategoryCommand;
import com.ojosama.eventservice.event.application.dto.result.EventCategoryResult;
import com.ojosama.eventservice.event.application.service.EventCategoryService;
import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import com.ojosama.eventservice.event.presentation.dto.request.CreateEventCategoryRequest;
import com.ojosama.eventservice.event.presentation.dto.response.EventCategoryResponse;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/event-categories")
public class EventCategoryController {

    private static final Set<String> ADMIN_ONLY = Set.of("ADMIN");

    private final EventCategoryService eventCategoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<EventCategoryResponse>> createCategory(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Valid @RequestBody CreateEventCategoryRequest request) {

        if (userId == null || userRole == null) {
            throw new CustomException(CommonErrorCode.INVALID_TOKEN);
        }
        if (!ADMIN_ONLY.contains(userRole)) {
            throw new EventException(EventErrorCode.EVENT_UNAUTHORIZED);
        }

        CreateEventCategoryCommand command = new CreateEventCategoryCommand(userId, request.name());
        EventCategoryResult result = eventCategoryService.createCategory(command);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(EventCategoryResponse.from(result)));
    }
}
