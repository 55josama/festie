package com.ojosama.eventservice.eventrequest.infrastructure.messaging.kafka.producer;

import com.ojosama.eventservice.eventrequest.domain.event.payload.EventRequestCreatedMessage;
import com.ojosama.eventservice.eventrequest.domain.event.payload.EventRequestProcessedMessage;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class KafkaEventRequestMessagePublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.event-request-created}")
    private String eventRequestCreatedTopic;

    public KafkaEventRequestMessagePublisher(
            @Qualifier("jsonKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishEventRequestCreated(EventRequestCreatedMessage message) {
        try {
            kafkaTemplate.send(eventRequestCreatedTopic, message.targetId().toString(), message)
                    .get(3, TimeUnit.SECONDS);
            log.info("[Kafka] 발행 성공: topic={}, targetId={}", eventRequestCreatedTopic, message.targetId());
        } catch (Exception e) {
            log.error("[Kafka] 발행 실패: topic={}, targetId={}", eventRequestCreatedTopic, message.targetId(), e);
        }
    }

}
