package com.ojosama.notificationservice.infrastructure.client.dto;

import java.util.List;
import java.util.UUID;

public record CalendarUserInfo(
        List<UUID> userIds
) {
}
