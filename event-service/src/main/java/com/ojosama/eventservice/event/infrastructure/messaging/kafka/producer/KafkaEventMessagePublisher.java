package com.ojosama.eventservice.event.infrastructure.messaging.kafka.producer;

import com.ojosama.eventservice.event.domain.event.payload.EventCreatedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventDeletedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventScheduleChangedMessage;
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
public class KafkaEventMessagePublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.event-created}")
    private String eventCreatedTopic;

    @Value("${spring.kafka.topic.event-deleted}")
    private String eventDeletedTopic;

    @Value("${spring.kafka.topic.event-changed}")
    private String eventChangedTopic;

    public KafkaEventMessagePublisher(@Qualifier("jsonKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishEventCreated(EventCreatedMessage message) {
        try {
            kafkaTemplate.send(eventCreatedTopic, message.eventId().toString(), message).get(3, TimeUnit.SECONDS);
            log.info("[Kafka] 발행 성공: topic={}, eventId={}", eventCreatedTopic, message.eventId());
        } catch (Exception e) {
            log.error("[Kafka] 발행 실패: topic={}, eventId={}", eventCreatedTopic, message.eventId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishEventDeleted(EventDeletedMessage message) {
        try {
            kafkaTemplate.send(eventDeletedTopic, message.eventId().toString(), message).get(3, TimeUnit.SECONDS);
            log.info("[Kafka] 발행 성공: topic={}, eventId={}", eventDeletedTopic, message.eventId());
        } catch (Exception e) {
            log.error("[Kafka] 발행 실패: topic={}, eventId={}", eventDeletedTopic, message.eventId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishScheduleChanged(EventScheduleChangedMessage message) {
        try {
            kafkaTemplate.send(eventChangedTopic, message.eventId().toString(), message).get(3, TimeUnit.SECONDS);
            log.info("[Kafka] 발행 성공: topic={}, eventId={}", eventChangedTopic, message.eventId());
        } catch (Exception e) {
            log.error("[Kafka] 발행 실패: topic={}, eventId={}", eventChangedTopic, message.eventId(), e);
        }
    }
}
