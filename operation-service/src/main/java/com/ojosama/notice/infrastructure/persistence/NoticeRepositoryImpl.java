package com.ojosama.notice.infrastructure.persistence;

import com.ojosama.notice.domain.model.entity.Notice;
import com.ojosama.notice.domain.repository.NoticeRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepository {
    private final NoticeJpaRepository noticeJpaRepository;

    @Override
    public Notice save(Notice notice){
        return noticeJpaRepository.save(notice);
    }

    @Override
    public Notice saveAndFlush(Notice notice){
        return noticeJpaRepository.saveAndFlush(notice);
    }

    @Override
    public Optional<Notice> findByIdAndDeletedAtIsNull(UUID id){
        return noticeJpaRepository.findById(id);
    }

    @Override
    public Page<Notice> findAllByDeletedAtIsNull(Pageable pageable){
        return noticeJpaRepository.findAll(pageable);
    }
}
