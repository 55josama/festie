package com.ojosama.eventservice.event.presentation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.eventservice.event.application.service.EventQueryService;
import com.ojosama.eventservice.event.presentation.dto.response.EventResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/events")
public class InternalEventController {

    private final EventQueryService eventQueryService;

    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(
            @PathVariable UUID eventId) {

        EventResponse response = EventResponse.from(eventQueryService.getEventById(eventId));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getEvents(
            @RequestParam(required = false) List<UUID> eventIds) {

        List<EventResponse> responses = (eventIds != null && !eventIds.isEmpty()
                ? eventQueryService.getEventsByIds(eventIds)
                : eventQueryService.getAllEvents())
                .stream().map(EventResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
