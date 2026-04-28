package com.ojosama.chatservice.presentation.dto.response;

import com.ojosama.chatservice.application.dto.result.ChatRoomSummaryResult;
import com.ojosama.chatservice.domain.model.ChatRoomStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChatRoomSummaryResponse(
        boolean chatRoomExists,
        UUID chatRoomId,
        ChatRoomStatus chatRoomStatus,
        LocalDateTime scheduledOpenAt,
        LocalDateTime scheduledCloseAt,
        LocalDateTime openedAt,
        LocalDateTime closedAt
) {
    public static ChatRoomSummaryResponse from(ChatRoomSummaryResult result) {
        return new ChatRoomSummaryResponse(
                result.chatRoomExists(),
                result.chatRoomId(),
                result.chatRoomStatus(),
                result.scheduledOpenAt(),
                result.scheduledCloseAt(),
                result.openedAt(),
                result.closedAt()
        );
    }
}
