package com.ojosama.report.domain.event.payload;

import com.ojosama.report.domain.model.enums.ReportTargetType;
import java.util.UUID;

public record TargetBlindEvent (
        UUID targetId,
        ReportTargetType reportTargetType,
        UUID targetUserId,
        String category,
        String reason
){ }
