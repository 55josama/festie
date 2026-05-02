package com.ojosama.eventservice.event.infrastructure.client.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatRoomSummaryDto(
        boolean chatRoomExists,
        UUID chatRoomId,
        String chatRoomStatus,
        LocalDateTime scheduledOpenAt,
        LocalDateTime scheduledCloseAt,
        LocalDateTime openedAt,
        LocalDateTime closedAt
) {
    public static ChatRoomSummaryDto empty() {
        return new ChatRoomSummaryDto(false, null, null, null, null, null, null);
    }
}
