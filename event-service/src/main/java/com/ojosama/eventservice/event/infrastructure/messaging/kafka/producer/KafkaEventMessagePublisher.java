package com.ojosama.eventservice.event.infrastructure.messaging.kafka.producer;

import com.ojosama.common.kafka.domain.OutboxEventPublisher;
import com.ojosama.eventservice.event.domain.event.EventMessagePublisher;
import com.ojosama.eventservice.event.domain.event.payload.EventCreatedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventDeletedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventScheduleChangedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventUpdatedMessage;
import com.ojosama.eventservice.event.infrastructure.messaging.kafka.KafkaTopicProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventMessagePublisher implements EventMessagePublisher {

    private static final String AGGREGATE_TYPE = "Event";

    private final OutboxEventPublisher outboxEventPublisher;

    private final KafkaTopicProperties topics;

    @Override
    public void publishEventCreated(EventCreatedMessage message) {
        outboxEventPublisher.publish(AGGREGATE_TYPE, message.eventId(),
                "EventCreated", topics.eventCreated(), message);
    }

    @Override
    public void publishEventDeleted(EventDeletedMessage message) {
        outboxEventPublisher.publish(AGGREGATE_TYPE, message.eventId(),
                "EventDeleted", topics.eventDeleted(), message);
    }

    @Override
    public void publishEventUpdated(EventUpdatedMessage message) {
        outboxEventPublisher.publish(AGGREGATE_TYPE, message.eventId(),
                "EventUpdated", topics.eventUpdated(), message);
    }

    @Override
    public void publishScheduleChanged(EventScheduleChangedMessage message) {
        outboxEventPublisher.publish(AGGREGATE_TYPE, message.eventId(),
                "EventScheduleChanged", topics.scheduleChanged(), message);
    }
}
