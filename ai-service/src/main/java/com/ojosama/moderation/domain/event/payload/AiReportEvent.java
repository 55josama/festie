package com.ojosama.moderation.domain.event.payload;

import java.util.UUID;

public record AiReportEvent(
    UUID targetId,
    UUID targetUserId,
    String targetType,
    String category,
    String description,
    String content
) { }
