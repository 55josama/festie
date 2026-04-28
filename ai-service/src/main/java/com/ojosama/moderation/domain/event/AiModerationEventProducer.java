package com.ojosama.moderation.domain.event;

import com.ojosama.moderation.domain.model.entity.AiModeration;

public interface AiModerationEventProducer {
    void publishEvaluatedEvent(AiModeration moderation);
}
