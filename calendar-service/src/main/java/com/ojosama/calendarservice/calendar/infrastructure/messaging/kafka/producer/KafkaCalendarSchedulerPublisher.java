package com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.producer;

import com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.producer.dto.EventImminentMessage;
import com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.producer.dto.TicketingImminentMessage;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaCalendarSchedulerPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaCalendarSchedulerPublisher(
            @Qualifier("jsonKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Value("${spring.kafka.topic.event-imminent}")
    private String eventImminentTopic;

    @Value("${spring.kafka.topic.ticketing-event-imminent}")
    private String ticketingEventImminentTopic;

    public void publishEventImminent(EventImminentMessage message) {
        try {
            kafkaTemplate.send(eventImminentTopic, message.eventId().toString(), message).get(3, TimeUnit.SECONDS);
            log.info("행사 임박 이벤트 발행 성공 : eventId={}", message.eventId());
        } catch (Exception e) {
            log.error("행사 임박 이벤트 발행 실패 : eventId={}", message.eventId());
        }
    }

    public void publishTicketingEventImminent(TicketingImminentMessage message) {
        try {
            kafkaTemplate.send(ticketingEventImminentTopic, message.eventId().toString(), message)
                    .get(3, TimeUnit.SECONDS);
            log.info("티켓팅 임박 이벤트 발행 성공 : eventId={}", message.eventId());
        } catch (Exception e) {
            log.error("티켓팅 임박 이벤트 발행 실패 : eventId={}", message.eventId());
        }

    }

}
