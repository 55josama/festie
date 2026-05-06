package com.ojosama.community.domain.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TargetUnblindedEvent(
        UUID targetId,
        TargetType targetType,
        String reason
) {
}
