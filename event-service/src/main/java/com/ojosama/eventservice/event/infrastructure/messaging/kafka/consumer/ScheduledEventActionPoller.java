package com.ojosama.eventservice.event.infrastructure.messaging.kafka.consumer;

import com.ojosama.common.kafka.domain.OutboxMessage;
import com.ojosama.common.kafka.domain.OutboxRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledEventActionPoller {

    private static final String SCHEDULED_EVENT_ACTION = "ScheduledEventAction";
    private static final int BATCH_SIZE = 100;

    private final OutboxRepository outboxRepository;
    private final ScheduledEventActionHandlerImpl handler;

    @Scheduled(fixedDelayString = "${festie.outbox.poll-delay-ms:1000}")
    @Transactional
    public void poll() {
        List<OutboxMessage> batch = outboxRepository.findPending(PageRequest.of(0, BATCH_SIZE));
        if (batch.isEmpty()) {
            return;
        }

        List<OutboxMessage> scheduledActions = batch.stream()
            .filter(msg -> SCHEDULED_EVENT_ACTION.equals(msg.getEventType()))
            .toList();

        if (scheduledActions.isEmpty()) {
            return;
        }

        log.debug("[ScheduledEventActionPoller] 처리 시작. count={}", scheduledActions.size());

        for (OutboxMessage message : scheduledActions) {
            try {
                handler.handle(message);
                message.markSent();
            } catch (Exception e) {
                log.error("[ScheduledEventActionPoller] 처리 실패. messageId={}, error={}",
                    message.getId(), e.getMessage(), e);
            }
        }

        outboxRepository.saveAll(scheduledActions);
    }
}
