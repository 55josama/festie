package com.ojosama.operationservice.domain.repository;

import com.ojosama.operationservice.domain.model.entity.Blacklist;
import com.ojosama.operationservice.domain.model.enums.BlacklistStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlacklistRepository {
    Blacklist save(Blacklist blacklist);

    Optional<Blacklist> findById(UUID id);

    Page<Blacklist> findAll(Pageable pageable);

    Page<Blacklist> findAllByStatus(BlacklistStatus status, Pageable pageable);

    boolean existsByUserIdAndStatus(UUID userId, BlacklistStatus status);
}
