package com.ojosama.operationservice.infrastructure.persistence;

import com.ojosama.operationservice.domain.model.entity.Notice;
import com.ojosama.operationservice.domain.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepository {
    private final NoticeJpaRepository noticeJpaRepository;

    @Override
    public Notice save(Notice notice){
        return noticeJpaRepository.save(notice);
    }
}
