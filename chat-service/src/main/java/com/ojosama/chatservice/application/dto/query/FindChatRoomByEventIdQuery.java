package com.ojosama.chatservice.application.dto.query;

import java.util.UUID;

public record FindChatRoomByEventIdQuery(
        UUID eventId
) {
}
