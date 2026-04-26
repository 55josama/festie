package com.ojosama.chatservice.application.dto.command;

import java.util.UUID;

public record CloseChatRoomCommand(
        UUID chatRoomId
) {
}
