package com.ojosama.notice.infrastructure.persistence;

import com.ojosama.notice.domain.model.entity.Notice;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeJpaRepository extends JpaRepository<Notice, UUID> {
}
