package com.ojosama.operationservice.infrastructure.persistence;

import com.ojosama.operationservice.domain.model.entity.Notice;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeJpaRepository extends JpaRepository<Notice, UUID> {
}
