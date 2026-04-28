package com.ojosama.blacklist.infrastructure.persistence;

import com.ojosama.blacklist.domain.model.entity.Blacklist;
import com.ojosama.blacklist.domain.model.enums.BlacklistStatus;
import com.ojosama.blacklist.domain.repository.BlacklistRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<Blacklist> findAll(Pageable pageable){
        return blacklistJpaRepository.findAll(pageable);
    }

    @Override
    public Page<Blacklist> findAllByStatus(BlacklistStatus status, Pageable pageable){
        return blacklistJpaRepository.findAllByStatus(status, pageable);
    }

    @Override
    public boolean existsByUserIdAndStatus(UUID userId, BlacklistStatus status){
        return blacklistJpaRepository.existsByUserIdAndStatus(userId, status);
    }
}
