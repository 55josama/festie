package com.ojosama.moderation.application.dto.result;

import com.ojosama.moderation.domain.model.entity.AiModeration;
import com.ojosama.moderation.domain.model.enums.ReportCategory;
import com.ojosama.moderation.domain.model.enums.TargetType;
import java.util.UUID;

public record AiModerationResult (
        UUID id,
        UUID targetId,
        UUID targetUserId,
        TargetType targetType,
        ReportCategory category,
        String contentSnapshot
){
    public static AiModerationResult from(AiModeration aiModeration) {
        return new AiModerationResult(
                aiModeration.getId(),
                aiModeration.getTargetId(),
                aiModeration.getTargetUserId(),
                aiModeration.getTargetType(),
                aiModeration.getCategory(),
                aiModeration.getContentSnapshot()
        );
    }
}
