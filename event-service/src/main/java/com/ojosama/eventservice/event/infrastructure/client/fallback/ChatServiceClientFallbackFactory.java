package com.ojosama.eventservice.event.infrastructure.client.fallback;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.eventservice.event.infrastructure.client.ChatServiceClient;
import com.ojosama.eventservice.event.infrastructure.client.dto.ChatRoomSummaryDto;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatServiceClientFallbackFactory implements FallbackFactory<ChatServiceClient> {

    @Override
    public ChatServiceClient create(Throwable cause) {
        return new ChatServiceClient() {
            @Override
            public ApiResponse<ChatRoomSummaryDto> getChatRoomSummary(UUID eventId) {
                log.warn("[Feign] chat-service 호출 실패, fallback 반환: eventId={}, cause={}", eventId, cause.getMessage());
                return ApiResponse.success(ChatRoomSummaryDto.empty());
            }
        };
    }
}
