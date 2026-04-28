package com.ojosama.blacklist.infrastructure.persistence;

import com.ojosama.blacklist.domain.model.entity.Blacklist;
import com.ojosama.blacklist.domain.model.enums.BlacklistStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistJpaRepository extends JpaRepository<Blacklist, UUID> {
    Optional<Blacklist> findById(UUID id);

    Page<Blacklist> findAll(Pageable pageable);

    Page<Blacklist> findAllByStatus(BlacklistStatus status, Pageable pageable);

    boolean existsByUserIdAndStatus(UUID userId, BlacklistStatus status);
}
