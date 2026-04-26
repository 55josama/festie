package com.ojosama.chatservice.application.dto.command;

import java.util.UUID;

public record ChangeChatRoomStatusCommand(
        UUID chatRoomId,
        ChatRoomStatusAction action,
        UUID adminId
) {
}
