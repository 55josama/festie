package com.ojosama.eventservice.event.infrastructure.messaging.kafka.producer;

import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.OutboxEventPublisher;
import com.ojosama.eventservice.event.domain.event.payload.EventCreatedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventDeletedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventScheduleChangedMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class KafkaEventMessagePublisher {

    private final OutboxEventPublisher outboxEventPublisher;

    @Value("${spring.kafka.topic.event-created:event.info.created.v1}")
    private String eventCreatedTopic;

    @Value("${spring.kafka.topic.event-deleted:event.info.deleted.v1}")
    private String eventDeletedTopic;

    @Value("${spring.kafka.topic.event-changed:event.schedule.changed.v1}")
    private String eventChangedTopic;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void publishEventCreated(EventCreatedMessage message) {
        outboxEventPublisher.publish("Event", message.eventId(), EventType.EVENT_CREATED, eventCreatedTopic, message);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void publishEventDeleted(EventDeletedMessage message) {
        outboxEventPublisher.publish("Event", message.eventId(), EventType.EVENT_DELETED, eventDeletedTopic, message);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void publishScheduleChanged(EventScheduleChangedMessage message) {
        outboxEventPublisher.publish("Event", message.eventId(), EventType.EVENT_SCHEDULE_CHANGED, eventChangedTopic,
                message);
    }
}
