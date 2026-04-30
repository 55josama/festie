package com.ojosama.community.domain.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

//블라인드 처리할 때 발행하는 이벤트.
//토픽: operation.report.blinded (yml: spring.kafka.topic.report-blinded)
@JsonIgnoreProperties(ignoreUnknown = true)
public record TargetBlindedEvent(
        UUID targetId,
        TargetType targetType,
        UUID targetUserId,
        UUID categoryId,
        String category
) {
}
