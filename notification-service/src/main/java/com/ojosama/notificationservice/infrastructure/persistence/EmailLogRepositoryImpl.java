package com.ojosama.notificationservice.infrastructure.persistence;

import com.ojosama.notificationservice.domain.repository.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EmailLogRepositoryImpl implements EmailLogRepository {

    private final JpaEmailLogRepository jpaEmailLogRepository;
}
