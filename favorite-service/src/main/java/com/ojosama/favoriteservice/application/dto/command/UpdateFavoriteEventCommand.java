package com.ojosama.favoriteservice.application.dto.command;

import com.ojosama.favoriteservice.infrastructure.messaging.kafka.dto.EventUpdatedMessage;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UpdateFavoriteEventCommand(
        UUID eventId,
        List<FieldChange> changedFields
) {
    public record FieldChange(
            String fieldName,
            String after
    ) {
    }

    public static UpdateFavoriteEventCommand from(EventUpdatedMessage message) {
        return UpdateFavoriteEventCommand.builder()
                .eventId(message.eventId())
                .changedFields(message.changedFields().stream()
                        .map(f -> new FieldChange(f.fieldName(), f.after()))
                        .toList())
                .build();
    }
}
