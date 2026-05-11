package com.ojosama.notificationservice.application.command;

import java.util.UUID;

public record TargetBlindEventCommand(
        UUID targetId,
        UUID targetUserId,
        String targetType,
        String category
) {
}
