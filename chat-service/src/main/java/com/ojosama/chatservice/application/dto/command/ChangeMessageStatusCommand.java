package com.ojosama.chatservice.application.dto.command;

import com.ojosama.chatservice.domain.model.MessageStatus;
import java.util.UUID;

public record ChangeMessageStatusCommand(
        UUID messageId,
        UUID adminId,
        MessageStatus status
) {
}

