package com.ojosama.moderation.domain.event.payload;

import com.ojosama.moderation.domain.model.entity.AiModeration;
import com.ojosama.moderation.domain.model.enums.ReportCategory;
import com.ojosama.moderation.domain.model.enums.TargetType;
import java.util.UUID;

public record AiEvaluateEvent(
        UUID targetId,
        UUID targetUserId,
        TargetType targetType,
        ReportCategory category,
        String content
) {
    public static AiEvaluateEvent from(AiModeration moderation) {
        return new AiEvaluateEvent(
                moderation.getTargetId(),
                moderation.getTargetUserId(),
                moderation.getTargetType(),
                moderation.getCategory(),
                moderation.getContent()
        );
    }
}
