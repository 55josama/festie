package com.ojosama.moderation.domain.repository;

import com.ojosama.moderation.domain.model.entity.AiModeration;
import java.util.List;

public interface AiModerationRepository {
    AiModeration save(AiModeration aiModeration);

    List<AiModeration> saveAll(List<AiModeration> moderations);
}
