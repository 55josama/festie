package com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.producer;

import com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.producer.dto.EventImminentMessage;
import com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.producer.dto.TicketingImminentMessage;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.OutboxEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaCalendarSchedulerPublisher {

    private final OutboxEventPublisher outboxEventPublisher;

    @Value("${spring.kafka.topic.event-imminent}")
    private String eventImminentTopic;

    @Value("${spring.kafka.topic.ticketing-event-imminent}")
    private String ticketingEventImminentTopic;

    public void publishEventImminent(EventImminentMessage message) {
        outboxEventPublisher.publish("Calendar", message.eventId(), EventType.SCHEDULE_REMINDER, eventImminentTopic,
                message);
    }

    public void publishTicketingEventImminent(TicketingImminentMessage message) {
        outboxEventPublisher.publish("Calendar", message.eventId(), EventType.TICKETING_SCHEDULE_REMINDER,
                ticketingEventImminentTopic, message);
    }

}
