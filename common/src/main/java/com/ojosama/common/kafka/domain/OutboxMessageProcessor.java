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

        for (Map.Entry<OutboxMessage, CompletableFuture<SendResult<String, String>>> entry
                : futures) {
            OutboxMessage message = entry.getKey();
            try {
                SendResult<String, String> result = entry.getValue().get(120, TimeUnit.SECONDS);
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
                message.markFailed("Timeout: 120초 내 응답 없음");
                log.warn("Outbox send timeout. id={}, retryCount={}",
                        message.getId(), message.getRetryCount());
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
