package com.ojosama.moderation.infrastructure.persistence;

import com.ojosama.moderation.domain.model.entity.AiModeration;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiModerationJpaRepository extends JpaRepository<AiModeration, UUID> {
}
