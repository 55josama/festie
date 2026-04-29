package com.ojosama.moderation.infrastructure.client.dto;

import com.ojosama.moderation.domain.model.enums.ReportCategory;
import java.util.Objects;
import java.util.UUID;

public record AiModerationClientResponse(
        UUID targetId,
        ReportCategory category
) {
    public AiModerationClientResponse {
        Objects.requireNonNull(targetId, "targetId must not be null");
        Objects.requireNonNull(category, "category must not be null");
    }
}
