package com.ojosama.notificationservice.domain.model.notification;

public record ChangedField(
        String fieldName,
        String before,
        String after
) {
}
