package com.ojosama.notificationservice.application.command;

import com.ojosama.notificationservice.domain.model.notification.ChangedField;
import java.util.List;
import java.util.UUID;

public record EventUpdatedCommand(
        UUID eventId,
        String eventName,
        List<ChangedField> changedFields,
        List<UUID> userIds
) {
}
