package com.ojosama.blacklist.application.service;

import com.ojosama.blacklist.application.dto.command.CreateBlacklistCommand;
import com.ojosama.blacklist.application.dto.command.UpdateBlacklistCommand;
import com.ojosama.blacklist.application.dto.query.ListBlacklistQuery;
import com.ojosama.blacklist.application.dto.result.BlacklistResult;
import com.ojosama.blacklist.domain.event.payload.BlacklistRegisterEvent;
import com.ojosama.blacklist.domain.event.payload.UserBlacklistStatusEvent;
import com.ojosama.blacklist.domain.exception.BlacklistErrorCode;
import com.ojosama.blacklist.domain.exception.BlacklistException;
import com.ojosama.blacklist.domain.model.entity.Blacklist;
import com.ojosama.blacklist.domain.model.enums.BlacklistStatus;
import com.ojosama.blacklist.domain.repository.BlacklistRepository;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.OutboxEventPublisher;
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
    private final OutboxEventPublisher outbox;

    // 블랙리스트 생성(관리자 수동 등록)
    @Transactional
    public BlacklistResult createBlacklistManual(CreateBlacklistCommand command) {
        validateNotAlreadyActive(command.userId());

        Blacklist savedBlacklist = saveAndPublish(command.userId(), command.reason());

        return BlacklistResult.from(savedBlacklist);
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

        publishStatusChangeEvent(blacklist.getUserId(), BlacklistStatus.INACTIVE);

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

    // 블랙리스트 저장 및 이벤트 발행
    private Blacklist saveAndPublish(UUID userId, String reason) {
        Blacklist blacklist = Blacklist.builder()
                .userId(userId)
                .reason(reason)
                .build();

        Blacklist saved = blacklistRepository.save(blacklist);

        // 유저 상태 변경 알림 (user-service에게 전달)
        publishStatusChangeEvent(saved.getUserId(), BlacklistStatus.ACTIVE);

        // 블랙리스트 등록 알림 (notification-service에게 전달)
        publishRegisterEvent(saved);

        return saved;
    }

    // 유저 블랙리스트 상태 변경 이벤트 발행
    private void publishStatusChangeEvent(UUID userId, BlacklistStatus status) {
        UserBlacklistStatusEvent event = new UserBlacklistStatusEvent(userId, status.name());

        outbox.publish(
                "BLACKLIST",
                userId,
                EventType.BLACKLIST_UPDATED, // EventType 상수 확인 필요
                "operation.blacklist.updated",
                event
        );
    }

    // 블랙리스트 신규 등록 알림 이벤트 발행
    private void publishRegisterEvent(Blacklist blacklist) {
        outbox.publish(
                "BLACKLIST",
                blacklist.getUserId(),
                EventType.BLACKLIST_REGISTERED,
                "operation.blacklist.registered",
                new BlacklistRegisterEvent(
                        blacklist.getUserId(),
                        blacklist.getReason()
                )
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
