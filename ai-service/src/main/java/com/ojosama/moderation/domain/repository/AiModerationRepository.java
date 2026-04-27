package com.ojosama.moderation.domain.repository;

import com.ojosama.moderation.domain.model.entity.AiModeration;

public interface AiModerationRepository {
    AiModeration save(AiModeration aiModeration);
}
