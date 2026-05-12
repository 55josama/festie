package com.ojosama.notificationservice.application.command;

import java.util.UUID;

public record EventRequestCommand(
        UUID targetId,
        String categoryName,
        String eventName
) {
}
