package com.ojosama.chatservice.application.dto.command;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChangeChatRoomScheduleCommand(
        UUID eventId,
        LocalDateTime eventStartAt,
        LocalDateTime eventEndAt
) {
}
