package com.ojosama.chatservice.presentation.dto.response;

import com.ojosama.chatservice.application.dto.result.PopularChatRoomResult;
import com.ojosama.chatservice.domain.model.ChatRoomStatus;
import com.ojosama.chatservice.domain.model.EventCategory;
import java.time.LocalDateTime;
import java.util.UUID;

public record PopularChatRoomResponse(
        UUID chatRoomId,
        UUID eventId,
        String eventName,
        EventCategory category,
        ChatRoomStatus status,
        LocalDateTime scheduledOpenAt,
        LocalDateTime scheduledCloseAt,
        LocalDateTime openedAt,
        LocalDateTime closedAt,
        int currentViewerCount
) {
    public static PopularChatRoomResponse from(PopularChatRoomResult result) {
        return new PopularChatRoomResponse(
                result.chatRoom().chatRoomId(),
                result.chatRoom().eventId(),
                result.chatRoom().eventName(),
                result.chatRoom().category(),
                result.chatRoom().status(),
                result.chatRoom().scheduledOpenAt(),
                result.chatRoom().scheduledCloseAt(),
                result.chatRoom().openedAt(),
                result.chatRoom().closedAt(),
                result.currentViewerCount()
        );
    }
}
