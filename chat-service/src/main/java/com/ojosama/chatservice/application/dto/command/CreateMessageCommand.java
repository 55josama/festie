package com.ojosama.chatservice.application.dto.command;

import java.util.UUID;

public record CreateMessageCommand(
        UUID chatRoomId,
        UUID userId,
        String writerNickname,
        String content
) {
}
