package com.ojosama.report.domain.event.payload;

import com.ojosama.report.domain.model.enums.TargetType;
import java.util.UUID;

public record TargetUnblindedEvent (
        UUID targetId,
        TargetType targetType,
        UUID targetUserId,
        String reason
){ }
