package com.ojosama.eventservice.event.infrastructure.client;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.eventservice.event.infrastructure.client.dto.ChatRoomSummaryDto;
import com.ojosama.eventservice.event.infrastructure.client.fallback.ChatServiceClientFallbackFactory;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "chat-service", fallbackFactory = ChatServiceClientFallbackFactory.class)
public interface ChatServiceClient {

    @GetMapping("/internal/v1/chat/rooms/event/{eventId}")
    ApiResponse<ChatRoomSummaryDto> getChatRoomSummary(@PathVariable("eventId") UUID eventId);
}
