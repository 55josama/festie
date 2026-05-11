package com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.producer;

import com.ojosama.calendarservice.calendar.domain.event.payload.CalendarEventDeletedMessage;
import com.ojosama.calendarservice.calendar.domain.event.payload.CalendarEventStatusUpdatedMessage;
import com.ojosama.calendarservice.calendar.domain.event.payload.CalendarEventUpdatedMessage;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.OutboxEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaCalendarProducer {

    private final OutboxEventPublisher outboxEventPublisher;

    @Value("${spring.kafka.topic.calendar-event-deleted}")
    private String deletedTopic;

    @Value("${spring.kafka.topic.calendar-event-updated}")
    private String updatedTopic;

    @Value("${spring.kafka.topic.calendar-event-status-updated}")
    private String statusUpdatedTopic;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void publishCalendarEventDeleted(CalendarEventDeletedMessage message) {
        outboxEventPublisher.publish("Calendar", message.eventId(), EventType.CALENDAR_DELETED, deletedTopic, message);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void publishCalendarEventUpdated(CalendarEventUpdatedMessage message) {
        outboxEventPublisher.publish("Calendar", message.eventId(), EventType.CALENDAR_UPDATED, updatedTopic, message);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void publishCalendarEventStatusUpdated(CalendarEventStatusUpdatedMessage message) {
        outboxEventPublisher.publish("Calendar", message.eventId(), EventType.CALENDAR_STATUS_UPDATED,
                statusUpdatedTopic,
                message);
    }
}
