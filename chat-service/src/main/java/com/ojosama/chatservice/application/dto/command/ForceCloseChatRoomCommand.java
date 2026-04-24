package com.ojosama.chatservice.application.dto.command;

import java.util.UUID;

public record ForceCloseChatRoomCommand(
        UUID chatRoomId,
        UUID adminId
) {
}
