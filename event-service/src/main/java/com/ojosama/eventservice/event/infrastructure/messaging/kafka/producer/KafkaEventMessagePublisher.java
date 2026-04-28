package com.ojosama.eventservice.event.infrastructure.messaging.kafka.producer;

import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.OutboxEventPublisher;
import com.ojosama.eventservice.event.domain.event.EventMessagePublisher;
import com.ojosama.eventservice.event.domain.event.payload.EventCreatedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventDeletedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventScheduleChangedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventUpdatedMessage;
import com.ojosama.eventservice.event.infrastructure.messaging.kafka.KafkaTopicProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!local")
@RequiredArgsConstructor
public class KafkaEventMessagePublisher implements EventMessagePublisher {

    private static final String AGGREGATE_TYPE = "Event";

    private final OutboxEventPublisher outboxEventPublisher;
    private final KafkaTopicProperties topics;

    @Override
    public void publishEventCreated(EventCreatedMessage message) {
        log.info(
                "[Kafka] Outbox 저장 → topic={}, eventType=EventCreated | eventId={}, name={}, category={}, period=[{} ~ {}]",
                topics.eventCreated(), message.eventId(), message.eventName(),
                message.categoryName(), message.eventStartAt(), message.eventEndAt());
        outboxEventPublisher.publish(AGGREGATE_TYPE, message.eventId(),
                EventType.EVENT_CREATED, topics.eventCreated(), message);
    }

    @Override
    public void publishEventDeleted(EventDeletedMessage message) {
        log.info("[Kafka] Outbox 저장 → topic={}, eventType=EventDeleted | eventId={}, name={}",
                topics.eventDeleted(), message.eventId(), message.eventName());
        outboxEventPublisher.publish(AGGREGATE_TYPE, message.eventId(),
                EventType.EVENT_DELETED, topics.eventDeleted(), message);
    }

    @Override
    public void publishEventUpdated(EventUpdatedMessage message) {
        log.info("[Kafka] Outbox 저장 → topic={}, eventType=EventUpdated | eventId={}, name={}",
                topics.eventUpdated(), message.eventId(), message.eventName());
        outboxEventPublisher.publish(AGGREGATE_TYPE, message.eventId(),
                EventType.EVENT_UPDATED, topics.eventUpdated(), message);
    }

    @Override
    public void publishScheduleChanged(EventScheduleChangedMessage message) {
        log.info("[Kafka] Outbox 저장 → topic={}, eventType=EventScheduleChanged | eventId={}, name={}, changedFields={}",
                topics.scheduleChanged(), message.eventId(), message.eventName(), message.changedFields());
        outboxEventPublisher.publish(AGGREGATE_TYPE, message.eventId(),
                EventType.EVENT_SCHEDULE_CHANGED, topics.scheduleChanged(), message);
    }
}
