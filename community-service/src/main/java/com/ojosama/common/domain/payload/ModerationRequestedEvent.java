package com.ojosama.common.domain.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

//AI 모더레이션 요청 이벤트.
//토픽: community.moderation.requested.v1
@JsonIgnoreProperties(ignoreUnknown = true)
public record ModerationRequestedEvent(
        UUID targetId,
        UUID targetUserId,
        TargetType targetType,
        String content
) {
    public static ModerationRequestedEvent of(
            UUID targetId, UUID targetUserId, TargetType targetType, String content) {
        return new ModerationRequestedEvent(targetId, targetUserId, targetType, content);
    }
}
