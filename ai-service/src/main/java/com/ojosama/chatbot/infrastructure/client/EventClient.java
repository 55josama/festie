package com.ojosama.chatbot.infrastructure.client;

import com.ojosama.chatbot.infrastructure.client.dto.EventClientResponse;
import com.ojosama.common.response.ApiResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service")
public interface EventClient {
    @GetMapping("/internal/v1/events")
    ApiResponse<List<EventClientResponse>> getEvents();
}
