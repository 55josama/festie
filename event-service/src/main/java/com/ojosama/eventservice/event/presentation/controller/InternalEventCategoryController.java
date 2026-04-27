package com.ojosama.eventservice.event.presentation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.eventservice.event.application.service.EventCategoryQueryService;
import com.ojosama.eventservice.event.presentation.dto.response.EventCategoryResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/event-categories")
public class InternalEventCategoryController {

    private final EventCategoryQueryService eventCategoryQueryService;

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<EventCategoryResponse>> getCategoryById(
            @PathVariable UUID categoryId) {

        EventCategoryResponse response = EventCategoryResponse.from(
                eventCategoryQueryService.getCategoryById(categoryId));
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
