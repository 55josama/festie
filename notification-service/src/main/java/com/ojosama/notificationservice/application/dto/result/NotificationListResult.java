package com.ojosama.notificationservice.application.dto.result;

import java.util.List;

public record NotificationListResult(
        List<NotificationResult> notifications,
        int count
) {
}
