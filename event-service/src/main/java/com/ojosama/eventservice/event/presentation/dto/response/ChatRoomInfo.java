package com.ojosama.eventservice.event.presentation.dto.response;

import com.ojosama.eventservice.event.infrastructure.client.dto.ChatRoomSummaryDto;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChatRoomInfo(
        boolean chatRoomExists,
        UUID chatRoomId,
        String chatRoomStatus,
        LocalDateTime scheduledOpenAt,
        LocalDateTime scheduledCloseAt,
        LocalDateTime openedAt,
        LocalDateTime closedAt
) {
    public static ChatRoomInfo from(ChatRoomSummaryDto dto) {
        return new ChatRoomInfo(
                dto.chatRoomExists(),
                dto.chatRoomId(),
                dto.chatRoomStatus(),
                dto.scheduledOpenAt(),
                dto.scheduledCloseAt(),
                dto.openedAt(),
                dto.closedAt()
        );
    }
}
