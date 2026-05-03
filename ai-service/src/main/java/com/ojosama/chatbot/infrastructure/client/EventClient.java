package com.ojosama.chatbot.infrastructure.client;

import com.ojosama.chatbot.infrastructure.client.dto.EventClientResponse;
import com.ojosama.common.response.ApiResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "event-service")
public interface EventClient {
    // 전체 행사 목록 조회
    @GetMapping("/internal/v1/events")
    List<EventClientResponse> getEvents();

    // 행사 리스트 상세 조회
    @GetMapping("/internal/v1/events")
    List<EventClientResponse> getEventsByIds(@RequestParam("eventIds") List<UUID> eventIds);

    // 단일 행사 상세 조회
    @GetMapping("/internal/v1/events/{eventId}")
    EventClientResponse getEventById(@PathVariable("eventId") UUID eventId);
}
