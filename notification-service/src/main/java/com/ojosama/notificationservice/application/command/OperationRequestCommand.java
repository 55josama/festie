package com.ojosama.notificationservice.application.command;

import java.util.UUID;

public record OperationRequestCommand(
        UUID requestId,
        UUID requesterId,
        String title
) {
}
