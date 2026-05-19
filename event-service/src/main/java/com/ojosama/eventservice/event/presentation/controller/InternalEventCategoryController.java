package com.ojosama.eventservice.event.presentation.controller;

import com.ojosama.eventservice.event.application.service.EventCategoryQueryService;
import com.ojosama.eventservice.event.presentation.dto.response.EventCategoryResponse;
import io.swagger.v3.oas.annotations.Hidden;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/event-categories")
public class InternalEventCategoryController {

    private final EventCategoryQueryService eventCategoryQueryService;

    @GetMapping("/{categoryId}")
    public ResponseEntity<EventCategoryResponse> getCategoryById(
            @PathVariable UUID categoryId) {

        EventCategoryResponse response = EventCategoryResponse.from(
                eventCategoryQueryService.getCategoryById(categoryId));
        return ResponseEntity.ok(response);
    }
}
