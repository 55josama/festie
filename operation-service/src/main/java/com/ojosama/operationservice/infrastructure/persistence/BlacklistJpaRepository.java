package com.ojosama.operationservice.infrastructure.persistence;

import com.ojosama.operationservice.domain.model.entity.Blacklist;
import com.ojosama.operationservice.domain.model.enums.BlacklistStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistJpaRepository extends JpaRepository<Blacklist, UUID> {
    Optional<Blacklist> findById(UUID id);

    boolean existsByUserIdAndStatus(UUID userId, BlacklistStatus status);
}
