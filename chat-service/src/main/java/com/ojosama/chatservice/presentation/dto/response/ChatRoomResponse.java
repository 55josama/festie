package com.ojosama.chatservice.presentation.dto.response;

import com.ojosama.chatservice.application.dto.result.ChatRoomResult;
import com.ojosama.chatservice.domain.model.ChatRoomStatus;
import com.ojosama.chatservice.domain.model.EventCategory;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChatRoomResponse(
        UUID chatRoomId,
        UUID eventId,
        String eventName,
        EventCategory category,
        ChatRoomStatus status,
        LocalDateTime scheduledOpenAt,
        LocalDateTime scheduledCloseAt,
        LocalDateTime openedAt,
        LocalDateTime closedAt
) {
    public static ChatRoomResponse from(ChatRoomResult result) {
        return new ChatRoomResponse(
                result.chatRoomId(),
                result.eventId(),
                result.eventName(),
                result.category(),
                result.status(),
                result.scheduledOpenAt(),
                result.scheduledCloseAt(),
                result.openedAt(),
                result.closedAt()
        );
    }
}
