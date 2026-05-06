package com.ojosama.notice.infrastructure.persistence;

import com.ojosama.notice.domain.model.entity.Notice;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeJpaRepository extends JpaRepository<Notice, UUID> {
    Optional<Notice> findByIdAndDeletedAtIsNull(UUID id);

    Page<Notice> findAllByDeletedAtIsNull(Pageable pageable);
}
