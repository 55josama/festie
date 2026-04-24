package com.ojosama.operationservice.infrastructure.persistence;

import com.ojosama.operationservice.domain.model.entity.Blacklist;
import com.ojosama.operationservice.domain.repository.BlacklistRepository;
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
}
