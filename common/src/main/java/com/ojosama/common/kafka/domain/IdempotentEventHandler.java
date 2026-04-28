package com.ojosama.common.kafka.domain;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

//컨슈머의 멱등 처리 헬퍼
//messageKey 중복 체크 — 이미 있으면 SKIP.
//같은 트랜잭션에 inbox 행 INSERT — 다음에 같은 메시지 와도 중복 처리 방지
//비즈니스 로직과 inbox 기록이 같은 트랜잭션에 있어야 멱등성이 보장됩니다!

//멱등성 체크와 코드 공부를 위해 수동 헬퍼 방식 선택(Aspect (AOP), Interceptor 방식 전환 논의)
@Component
@RequiredArgsConstructor
@Slf4j
@Transactional(noRollbackFor = DataIntegrityViolationException.class)
public class IdempotentEventHandler {

    private final InboxRepository inboxRepository;

    public void handle(
            UUID messageKey,
            String consumerGroup,
            String topic,
            String eventType,
            Runnable businessLogic) {

        if (inboxRepository.existsByMessageKeyAndConsumerGroup(messageKey, consumerGroup)) {
            log.debug("이미 처리된 메시지 SKIP. key={}, group={}", messageKey, consumerGroup);
            return;
        }

        try {
            inboxRepository.save(InboxMessage.of(messageKey, consumerGroup, topic, eventType));
        } catch (DataIntegrityViolationException e) {
            // 동시 처리 중 다른 컨슈머가 먼저 inbox에 기록한 경우 (드물지만 가능)
            log.debug("inbox 동시 INSERT 충돌. 다른 컨슈머가 처리 완료한 것으로 간주. key={}", messageKey);
            return;
        }

        businessLogic.run();
    }
}
