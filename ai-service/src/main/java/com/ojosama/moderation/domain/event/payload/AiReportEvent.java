package com.ojosama.moderation.domain.event.payload;

import com.ojosama.moderation.domain.model.enums.TargetType;
import java.util.UUID;

public record AiReportEvent(
        UUID targetId,
        UUID targetUserId,
        TargetType targetType,
        String content
) { }
