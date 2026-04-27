package com.ojosama.moderation.infrastructure.persistence;

import com.ojosama.moderation.domain.model.entity.AiModeration;
import com.ojosama.moderation.domain.repository.AiModerationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AiModerationRepositoryImpl implements AiModerationRepository {
    private final AiModerationJpaRepository aiModerationJpaRepository;

    @Override
    public AiModeration save(AiModeration aiModeration){
        return aiModerationJpaRepository.save(aiModeration);
    }
}
