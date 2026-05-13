package com.ojosama.eventservice.event.presentation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.eventservice.event.application.dto.command.CreateEventCategoryCommand;
import com.ojosama.eventservice.event.application.dto.command.UpdateEventCategoryCommand;
import com.ojosama.eventservice.event.application.dto.result.EventCategoryResult;
import com.ojosama.eventservice.event.application.service.EventCategoryCommandService;
import com.ojosama.eventservice.event.application.service.EventCategoryQueryService;
import com.ojosama.eventservice.event.presentation.dto.request.CreateEventCategoryRequest;
import com.ojosama.eventservice.event.presentation.dto.request.UpdateEventCategoryRequest;
import com.ojosama.eventservice.event.presentation.dto.response.EventCategoryResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/event-categories")
public class EventCategoryController {

    private final EventCategoryCommandService eventCategoryCommandService;
    private final EventCategoryQueryService eventCategoryQueryService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<EventCategoryResponse>>> getCategories() {

        List<EventCategoryResponse> response = eventCategoryQueryService.getCategories().stream()
                .map(EventCategoryResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventCategoryResponse>> createCategory(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreateEventCategoryRequest request) {

        CreateEventCategoryCommand command = new CreateEventCategoryCommand(UUID.fromString(userId), request.name());
        EventCategoryResult result = eventCategoryCommandService.createCategory(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(EventCategoryResponse.from(result)));
    }

    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventCategoryResponse>> updateCategory(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateEventCategoryRequest request) {

        UpdateEventCategoryCommand command = new UpdateEventCategoryCommand(UUID.fromString(userId), categoryId, request.name());
        EventCategoryResult result = eventCategoryCommandService.updateCategory(command);
        return ResponseEntity.ok(ApiResponse.success(EventCategoryResponse.from(result)));
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID categoryId) {

        eventCategoryCommandService.deleteCategory(UUID.fromString(userId), categoryId);
        return ResponseEntity.noContent().build();
    }
}
