package com.ojosama.common.kafka.domain;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Qualifier;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OutboxMessageProcessor {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxStatusPersister outboxStatusPersister;

    public OutboxMessageProcessor(
            @Qualifier("kafkaTemplate") KafkaTemplate<String, String> kafkaTemplate,
            OutboxStatusPersister outboxStatusPersister) {
        this.kafkaTemplate = kafkaTemplate;
        this.outboxStatusPersister = outboxStatusPersister;
    }

    public void sendBatch(List<OutboxMessage> messages) {
        List<Map.Entry<OutboxMessage, CompletableFuture<SendResult<String, String>>>> futures =
                messages.stream()
                        .map(message -> Map.entry(
                                message,
                                kafkaTemplate.send(
                                        message.getTopic(),
                                        message.getMessageKey().toString(),
                                        message.getPayload()
                                )
                        ))
                        .toList();

        boolean interrupted = false; //인터럽트 발생 여부 기록
        long deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(120);

        for (Map.Entry<OutboxMessage, CompletableFuture<SendResult<String, String>>> entry
                : futures) {
            OutboxMessage message = entry.getKey();
            try {
                long remainingNanos = deadlineNanos - System.nanoTime();
                if (remainingNanos <= 0) {
                    message.markFailed("Timeout: 배치 처리 제한 시간 초과");
                    continue;
                }
                SendResult<String, String> result =
                        entry.getValue().get(remainingNanos, TimeUnit.NANOSECONDS);
                log.debug("Outbox sent. id={}, topic={}, partition={}, offset={}",
                        message.getId(),
                        message.getTopic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                message.markSent();

            } catch (InterruptedException e) {
                interrupted = true; //break 대신 플래그만 설정
                message.markFailed("Interrupted: " + e.getMessage());
                log.warn("Outbox send interrupted. id={}, retryCount={}",
                        message.getId(), message.getRetryCount());

            } catch (ExecutionException e) {
                message.markFailed(
                        e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                log.warn("Outbox send failed. id={}, retryCount={}, error={}",
                        message.getId(), message.getRetryCount(), message.getLastError());

            } catch (TimeoutException e) {
                interrupted = true;

                //future가 이미 완료됐을 수 있으니 한 번 더 확인
                CompletableFuture<SendResult<String, String>> future = entry.getValue();

                if (future.isDone() && !future.isCompletedExceptionally()) {
                    // send가 실제로 성공했으면 markSent
                    try {
                        SendResult<String, String> result = future.getNow(null);
                        if (result != null) {
                            message.markSent();
                            log.debug("Outbox sent (after interrupt). id={}, topic={}",
                                    message.getId(), message.getTopic());
                        } else {
                            message.markFailed("Interrupted: future 결과 null");
                        }
                    } catch (Exception ex) {
                        message.markFailed("Interrupted + 결과 확인 실패: " + ex.getMessage());
                    }
                } else if (future.isCompletedExceptionally()) {
                    // send가 실패했으면 markFailed
                    message.markFailed("Interrupted + send 실패");
                    log.warn("Outbox send failed (after interrupt). id={}, retryCount={}",
                            message.getId(), message.getRetryCount());
                } else {
                    // future가 아직 진행 중 — 결과를 모르니 PENDING 유지 (markFailed 안 함)
                    // 다음 poll에서 다시 처리됨 → inbox 멱등으로 중복 안전
                    log.warn("Outbox send interrupted, future still pending. id={}", message.getId());
                }
            }
        }

        // 모든 future 결과 확인 후 한 번에 저장
        outboxStatusPersister.persist(messages);

        // 인터럽트 상태 복원 — 루프 끝난 후에 설정
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
