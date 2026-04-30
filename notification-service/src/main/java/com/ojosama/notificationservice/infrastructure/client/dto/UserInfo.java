package com.ojosama.notificationservice.infrastructure.client.dto;

import java.util.Map;
import java.util.UUID;

public record UserInfo(
        Map<UUID, String> userInfo
) {
}
