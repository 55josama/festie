package com.ojosama.eventservice.eventrequest.infrastructure.messaging.kafka.producer;

import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.OutboxEventPublisher;
import com.ojosama.eventservice.eventrequest.domain.event.payload.EventRequestCreatedMessage;
import com.ojosama.eventservice.eventrequest.domain.event.payload.EventRequestProcessedMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class KafkaEventRequestMessagePublisher {

    private final OutboxEventPublisher outboxEventPublisher;

    @Value("${spring.kafka.topic.event-request-created:event.request.created.v1}")
    private String eventRequestCreatedTopic;

    @Value("${spring.kafka.topic.event-request-created-result:event.request.processed.v1}")
    private String eventRequestCreatedResultTopic;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void publishEventRequestCreated(EventRequestCreatedMessage message) {
        outboxEventPublisher.publish("EventRequest", message.targetId(),
                EventType.EVENT_REQUEST_CREATED, eventRequestCreatedTopic, message);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void publishEventRequestProcessed(EventRequestProcessedMessage message) {
        outboxEventPublisher.publish("EventRequest", message.targetId(),
                EventType.EVENT_REQUEST_RESULT, eventRequestCreatedResultTopic, message);
    }
}
