package com.ojosama.notificationservice.application.command;

import java.util.UUID;

public record BlackListRequestCommand(
        UUID targetUserId,
        String reason,
        long blindCount
) {
}
