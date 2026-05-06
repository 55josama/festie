package com.ojosama.notice.domain.repository;

import com.ojosama.notice.domain.model.entity.Notice;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoticeRepository {
    Notice save(Notice notice);

    Notice saveAndFlush(Notice notice);

    Optional<Notice> findByIdAndDeletedAtIsNull(UUID id);

    Page<Notice> findAllByDeletedAtIsNull(Pageable pageable);
}
