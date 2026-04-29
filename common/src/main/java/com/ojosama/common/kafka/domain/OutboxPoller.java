package com.ojosama.common.kafka.domain;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//Outbox 폴러. 짧은 주기로 PENDING 메시지를 읽어 Kafka로 발행한다.
//최대 재시도 초과 시 FAILED로 마킹 — 운영 알림은 추후 고려/지금은 모니터링으로 확인하는 것으로 구현
//현재는 컨슈머의 inbox 멱등 처리로 중복 발행을 막고, 추후 SELECT ... FOR UPDATE SKIP LOCKED로 락을 걸어 1회 발행을 보장하는 것으로 구현
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {

    private static final int BATCH_SIZE = 100;

    private final OutboxRepository outboxRepository;
    private final OutboxMessageProcessor processor;

    @Scheduled(fixedDelayString = "${festie.outbox.poll-delay-ms:1000}")
    public void poll() {
        List<OutboxMessage> batch = outboxRepository.findPending(PageRequest.of(0, BATCH_SIZE));
        if (batch.isEmpty()) {
            return;
        }
        log.debug("Outbox 배치 처리 시작. count={}", batch.size());

        // 기존: 건별 루프 → 변경: 배치 한 번에 처리
        processor.sendBatch(batch);
    }
}
