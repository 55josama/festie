package com.ojosama.chatservice.application.dto.query;

import java.util.UUID;

public record FindMessagesByChatRoomQuery(
        UUID chatRoomId,
        int page,
        int size
) {
}
