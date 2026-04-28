package com.ojosama.chatservice.application.dto.result;

import com.ojosama.chatservice.domain.model.ChatRoom;
import com.ojosama.chatservice.domain.model.ChatRoomStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChatRoomSummaryResult(
        boolean chatRoomExists,
        UUID chatRoomId,
        ChatRoomStatus chatRoomStatus,
        LocalDateTime scheduledOpenAt,
        LocalDateTime scheduledCloseAt,
        LocalDateTime openedAt,
        LocalDateTime closedAt
) {
    public static ChatRoomSummaryResult from(ChatRoom chatRoom) {
        return new ChatRoomSummaryResult(
                true,
                chatRoom.getId(),
                chatRoom.getStatus(),
                chatRoom.getSchedule().getScheduledOpenAt(),
                chatRoom.getSchedule().getScheduledCloseAt(),
                chatRoom.getOpenedAt(),
                chatRoom.getClosedAt()
        );
    }

    public static ChatRoomSummaryResult empty() {
        return new ChatRoomSummaryResult(
                false,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
