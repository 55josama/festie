package com.ojosama.operationservice.infrastructure.persistence;

import com.ojosama.operationservice.domain.model.entity.Blacklist;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistJpaRepository extends JpaRepository<Blacklist, UUID> {
}
