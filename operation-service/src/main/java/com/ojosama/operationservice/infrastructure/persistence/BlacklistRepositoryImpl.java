package com.ojosama.operationservice.infrastructure.persistence;

import com.ojosama.operationservice.domain.model.entity.Blacklist;
import com.ojosama.operationservice.domain.model.enums.BlacklistStatus;
import com.ojosama.operationservice.domain.repository.BlacklistRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BlacklistRepositoryImpl implements BlacklistRepository {
    private final BlacklistJpaRepository blacklistJpaRepository;

    @Override
    public Blacklist save(Blacklist blacklist){
        return blacklistJpaRepository.save(blacklist);
    }

    @Override
    public Optional<Blacklist> findById(UUID id){
        return blacklistJpaRepository.findById(id);
    }

    @Override
    public boolean existsByUserIdAndStatus(UUID userId, BlacklistStatus status){
        return blacklistJpaRepository.existsByUserIdAndStatus(userId, status);
    }
}
