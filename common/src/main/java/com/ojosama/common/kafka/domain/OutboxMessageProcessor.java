package com.ojosama.common.kafka.domain;

import org.springframework.beans.factory.annotation.Qualifier;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

//Outbox 메시지 한 건을 별도 트랜잭션으로 처리.
//REQUIRES_NEW로 분리한 이유: 폴러 루프 내에서 한 메시지의 실패가
//다음 메시지 처리에 영향을 주지 않도록 하기 위함.
@Component
@Slf4j
public class OutboxMessageProcessor {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxMessageProcessor(
            @Qualifier("kafkaTemplate") KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void send(OutboxMessage message) {
        try {
            // messageKey를 Kafka key로 사용 — 같은 키는 같은 파티션에 들어가
            // 순서 보장 + 컨슈머 측 멱등 처리에 활용됨
            SendResult<String, String> result = kafkaTemplate.send(
                    message.getTopic(),
                    message.getMessageKey().toString(),
                    message.getPayload()
            ).get();

            log.debug("Outbox sent. id={}, topic={}, partition={}, offset={}",
                    message.getId(),
                    message.getTopic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

            message.markSent();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            message.markFailed("Interrupted: " + e.getMessage());
            log.warn("Outbox send interrupted. id={}, retryCount={}", message.getId(), message.getRetryCount());
        } catch (ExecutionException e) {
            message.markFailed(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            log.warn("Outbox send failed. id={}, retryCount={}, error={}",
                    message.getId(), message.getRetryCount(), message.getLastError());
        }
    }
}
