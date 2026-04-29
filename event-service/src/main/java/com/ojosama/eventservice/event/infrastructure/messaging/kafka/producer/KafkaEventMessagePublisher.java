package com.ojosama.eventservice.event.infrastructure.messaging.kafka.producer;

import com.ojosama.eventservice.event.domain.event.EventMessagePublisher;
import com.ojosama.eventservice.event.domain.event.payload.EventCreatedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventDeletedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventScheduleChangedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventUpdatedMessage;
import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaEventMessagePublisher implements EventMessagePublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.event-created}")
    private String eventCreatedTopic;

    @Value("${spring.kafka.topic.event-deleted}")
    private String eventDeletedTopic;

    @Value("${spring.kafka.topic.event-updated}")
    private String eventUpdatedTopic;

    @Value("${spring.kafka.topic.schedule-changed}")
    private String scheduleChangedTopic;

    public KafkaEventMessagePublisher(@Qualifier("jsonKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishEventCreated(EventCreatedMessage message) {
        try {
            kafkaTemplate.send(eventCreatedTopic, message.eventId().toString(), message).get(3, TimeUnit.SECONDS);
            log.info("[Kafka] 발행 성공: topic={}, eventId={}", eventCreatedTopic, message.eventId());
        } catch (Exception e) {
            log.error("[Kafka] 발행 실패: topic={}, eventId={}", eventCreatedTopic, message.eventId(), e);
            throw new EventException(EventErrorCode.EVENT_PUBLISH_FAILED);
        }
    }

    @Override
    public void publishEventDeleted(EventDeletedMessage message) {
        try {
            kafkaTemplate.send(eventDeletedTopic, message.eventId().toString(), message).get(3, TimeUnit.SECONDS);
            log.info("[Kafka] 발행 성공: topic={}, eventId={}", eventDeletedTopic, message.eventId());
        } catch (Exception e) {
            log.error("[Kafka] 발행 실패: topic={}, eventId={}", eventDeletedTopic, message.eventId(), e);
            throw new EventException(EventErrorCode.EVENT_PUBLISH_FAILED);
        }
    }

    @Override
    public void publishEventUpdated(EventUpdatedMessage message) {
        try {
            kafkaTemplate.send(eventUpdatedTopic, message.eventId().toString(), message).get(3, TimeUnit.SECONDS);
            log.info("[Kafka] 발행 성공: topic={}, eventId={}", eventUpdatedTopic, message.eventId());
        } catch (Exception e) {
            log.error("[Kafka] 발행 실패: topic={}, eventId={}", eventUpdatedTopic, message.eventId(), e);
            throw new EventException(EventErrorCode.EVENT_PUBLISH_FAILED);
        }
    }

    @Override
    public void publishScheduleChanged(EventScheduleChangedMessage message) {
        try {
            kafkaTemplate.send(scheduleChangedTopic, message.eventId().toString(), message).get(3, TimeUnit.SECONDS);
            log.info("[Kafka] 발행 성공: topic={}, eventId={}", scheduleChangedTopic, message.eventId());
        } catch (Exception e) {
            log.error("[Kafka] 발행 실패: topic={}, eventId={}", scheduleChangedTopic, message.eventId(), e);
            throw new EventException(EventErrorCode.EVENT_PUBLISH_FAILED);
        }
    }
}
