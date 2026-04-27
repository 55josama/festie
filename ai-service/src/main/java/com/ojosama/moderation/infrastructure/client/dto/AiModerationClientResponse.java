package com.ojosama.moderation.infrastructure.client.dto;

import com.ojosama.moderation.domain.model.enums.ReportCategory;
import java.util.UUID;

public record AiModerationClientResponse(
        UUID targetId,
        ReportCategory category
) { }
