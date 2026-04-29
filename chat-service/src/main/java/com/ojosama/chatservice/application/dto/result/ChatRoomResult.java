package com.ojosama.chatservice.application.dto.result;

import com.ojosama.chatservice.domain.model.ChatRoom;
import com.ojosama.chatservice.domain.model.ChatRoomStatus;
import com.ojosama.chatservice.domain.model.EventCategory;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChatRoomResult(
        UUID chatRoomId,
        UUID eventId,
        String eventName,
        EventCategory category,
        ChatRoomStatus status,
        LocalDateTime scheduledOpenAt,
        LocalDateTime scheduledCloseAt,
        LocalDateTime openedAt,
        LocalDateTime closedAt,
        UUID changedBy
) {
    // service -> application/dto
    public static ChatRoomResult from(ChatRoom chatRoom) {
        return new ChatRoomResult(
                chatRoom.getId(),
                chatRoom.getEventId(),
                chatRoom.getEventName(),
                chatRoom.getCategory(),
                chatRoom.getStatus(),
                chatRoom.getSchedule().getScheduledOpenAt(),
                chatRoom.getSchedule().getScheduledCloseAt(),
                chatRoom.getOpenedAt(),
                chatRoom.getClosedAt(),
                chatRoom.getChangedBy()
        );
    }
}
