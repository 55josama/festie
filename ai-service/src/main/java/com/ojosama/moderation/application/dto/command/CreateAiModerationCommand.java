package com.ojosama.moderation.application.dto.command;

import com.ojosama.moderation.domain.model.entity.AiModeration;
import com.ojosama.moderation.domain.model.enums.ReportCategory;
import com.ojosama.moderation.domain.model.enums.TargetType;
import java.util.UUID;

public record CreateAiModerationCommand(
        UUID targetId,
        UUID targetUserId,
        TargetType targetType,
        ReportCategory category,
        String contentSnapshot
) {
    public AiModeration toEntity(){
        return AiModeration.of(
                targetId,
                targetUserId,
                targetType,
                category,
                contentSnapshot
        );
    }
}
