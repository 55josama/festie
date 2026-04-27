package com.ojosama.moderation.domain.event.payload;

import com.ojosama.moderation.domain.model.entity.AiModeration;
import com.ojosama.moderation.domain.model.enums.TargetType;
import java.util.UUID;

public record AiModerationRequestEvent (
        UUID targetId,
        UUID targetUserId,
        TargetType targetType,
        String content
){
    public static AiModerationRequestEvent from(AiModeration moderation) {
        return new AiModerationRequestEvent(
                moderation.getTargetId(),
                moderation.getTargetUserId(),
                moderation.getTargetType(),
                moderation.getContent()
        );
    }
}
