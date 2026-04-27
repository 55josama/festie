package com.ojosama.moderation.domain.event;

import com.ojosama.moderation.domain.model.entity.AiModeration;

public interface AiModerateEventProducer {
    void publishEvaluatedEvent(AiModeration moderation);
}
