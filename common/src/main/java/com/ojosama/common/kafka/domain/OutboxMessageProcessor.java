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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class OutboxMessageProcessor {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxMessageProcessor(
            @Qualifier("kafkaTemplate") KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // 배치 처리
    @Transactional(propagation = Propagation.REQUIRES_NEW)
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

        for (Map.Entry<OutboxMessage, CompletableFuture<SendResult<String, String>>> entry
                : futures) {
            OutboxMessage message = entry.getKey();
            try {
                SendResult<String, String> result = entry.getValue().get(5, TimeUnit.SECONDS);
                log.debug("Outbox sent. id={}, topic={}, partition={}, offset={}",
                        message.getId(),
                        message.getTopic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                message.markSent();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                message.markFailed("Interrupted: " + e.getMessage());
                log.warn("Outbox send interrupted. id={}, retryCount={}",
                        message.getId(), message.getRetryCount());
            } catch (ExecutionException e) {
                message.markFailed(
                        e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                log.warn("Outbox send failed. id={}, retryCount={}, error={}",
                        message.getId(), message.getRetryCount(), message.getLastError());
            } catch (TimeoutException e) {                          // ← 이거 추가
                message.markFailed("Timeout: 5초 내 응답 없음");
                log.warn("Outbox send timeout. id={}, retryCount={}",
                        message.getId(), message.getRetryCount());
            }
        }
    }
}
