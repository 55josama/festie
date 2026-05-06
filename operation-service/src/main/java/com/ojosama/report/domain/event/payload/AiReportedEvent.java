package com.ojosama.report.domain.event.payload;

import java.util.UUID;

public record AiReportedEvent(
        UUID targetId,
        UUID targetUserId,
        String targetType,
        String category,
        String content
) { }
