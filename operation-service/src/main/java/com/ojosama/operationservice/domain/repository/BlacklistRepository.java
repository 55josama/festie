package com.ojosama.operationservice.domain.repository;

import com.ojosama.operationservice.domain.model.entity.Blacklist;
import com.ojosama.operationservice.domain.model.enums.BlacklistStatus;
import java.util.Optional;
import java.util.UUID;

public interface BlacklistRepository {
    Blacklist save(Blacklist blacklist);

    Optional<Blacklist> findById(UUID id);

    boolean existsByUserIdAndStatus(UUID userId, BlacklistStatus status);
}
