package com.ojosama.chatservice.application.dto.command;

import com.ojosama.chatservice.domain.model.EventCategory;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateChatRoomCommand(
        UUID eventId,
        String eventName,
        EventCategory category,
        LocalDateTime scheduledOpenAt,
        LocalDateTime scheduledCloseAt
) {
}