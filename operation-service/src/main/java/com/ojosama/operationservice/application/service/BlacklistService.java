package com.ojosama.operationservice.application.service;

import com.ojosama.operationservice.application.dto.command.CreateBlacklistCommand;
import com.ojosama.operationservice.application.dto.command.UpdateBlacklistCommand;
import com.ojosama.operationservice.application.dto.query.ListBlacklistQuery;
import com.ojosama.operationservice.application.dto.result.BlacklistResult;
import com.ojosama.operationservice.domain.event.BlacklistEventProducer;
import com.ojosama.operationservice.domain.event.payload.UserBlacklistStatusEvent;
import com.ojosama.operationservice.domain.exception.BlacklistErrorCode;
import com.ojosama.operationservice.domain.exception.BlacklistException;
import com.ojosama.operationservice.domain.model.entity.Blacklist;
import com.ojosama.operationservice.domain.model.enums.BlacklistStatus;
import com.ojosama.operationservice.domain.repository.BlacklistRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlacklistService {
    private final BlacklistRepository blacklistRepository;
    private final BlacklistEventProducer blacklistEventProducer;

    // 블랙리스트 생성(관리자 수동 등록)
    @Transactional
    public BlacklistResult createBlacklistManual(CreateBlacklistCommand command) {
        validateNotAlreadyActive(command.userId());

        Blacklist savedBlacklist = blacklistRepository.save(command.toEntity());

        // 유저/인증 서버에 차단 알림 이벤트 발행
        publishStatusEvent(savedBlacklist.getUserId(), BlacklistStatus.ACTIVE);

        return BlacklistResult.from(savedBlacklist);
    }

    // 블랙리스트 생성(Kafka 자동 등록)
    @Transactional
    public void createBlacklistSafe(CreateBlacklistCommand command) {
        if (!blacklistRepository.existsByUserIdAndStatus(command.userId(), BlacklistStatus.ACTIVE)) {
            Blacklist savedBlacklist = blacklistRepository.save(command.toEntity());

            publishStatusEvent(savedBlacklist.getUserId(), BlacklistStatus.ACTIVE);
        }
    }

    // 블랙리스트 목록 조회
    public Page<BlacklistResult> getBlacklists(ListBlacklistQuery listBlacklistQuery, Pageable pageable) {
        Page<Blacklist> blacklists = fetchBlacklistsByQuery(listBlacklistQuery, pageable);
        return blacklists.map(BlacklistResult::from);
    }

    // 블랙리스트 해제
    @Transactional
    public BlacklistResult releaseBlacklist(UUID blacklistId, UpdateBlacklistCommand command) {
        Blacklist blacklist = findBlacklistById(blacklistId);

        validateStatusIsActive(blacklist);

        blacklist.release(command.reason());

        publishStatusEvent(blacklist.getUserId(), BlacklistStatus.INACTIVE);

        return BlacklistResult.from(blacklist);
    }

    private void validateNotAlreadyActive(UUID userId) {
        if (blacklistRepository.existsByUserIdAndStatus(userId, BlacklistStatus.ACTIVE)) {
            throw new BlacklistException(BlacklistErrorCode.USER_ALREADY_ACTIVATED);
        }
    }

    private Blacklist findBlacklistById(UUID id) {
        return blacklistRepository.findById(id)
                .orElseThrow(() -> new BlacklistException(BlacklistErrorCode.BLACKLIST_NOT_FOUND));
    }

    private void publishStatusEvent(UUID userId, BlacklistStatus status) {
        blacklistEventProducer.publishStatusChangeEvent(
                new UserBlacklistStatusEvent(userId, status.name())
        );
    }

    private Page<Blacklist> fetchBlacklistsByQuery(ListBlacklistQuery query, Pageable pageable) {
        if (query.blacklistStatus() != null) {
            return blacklistRepository.findAllByStatus(query.blacklistStatus(), pageable);
        }
        return blacklistRepository.findAll(pageable);
    }

    private void validateStatusIsActive(Blacklist blacklist){
        if (blacklist.getStatus() != BlacklistStatus.ACTIVE) {
            throw new BlacklistException(BlacklistErrorCode.BLACKLIST_NOT_ACTIVE);
        }
    }
}
