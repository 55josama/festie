package com.ojosama.chatservice.infrastructure.messaging.kafka.dto;

import com.ojosama.chatservice.domain.model.Message;
import java.util.UUID;

public record AiModerationRequestEvent(
        UUID targetId,
        UUID targetUserId,
        TargetType targetType,
        String content
) {
    public static AiModerationRequestEvent from(Message message) {
        return new AiModerationRequestEvent(
                message.getId(),
                message.getUserId(),
                TargetType.CHAT,
                message.getContent()
        );
    }
}
