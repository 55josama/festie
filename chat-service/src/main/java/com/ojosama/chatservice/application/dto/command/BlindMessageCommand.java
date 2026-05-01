package com.ojosama.chatservice.application.dto.command;

import java.util.UUID;

public record BlindMessageCommand(
        UUID messageId,
        UUID adminId
) {
}

