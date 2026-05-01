package com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.producer;

import com.ojosama.calendarservice.calendar.domain.exception.CalendarErrorCode;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarException;
import com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.producer.dto.CalendarEventDeletedMessage;
import com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.producer.dto.CalendarEventUpdatedMessage;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@Transactional
public class KafkaCalendarPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.calendar-event-deleted}")
    private String deletedTopic;

    @Value("${spring.kafka.topic.calendar-event-updated}")
    private String updatedTopic;


    public KafkaCalendarPublisher(
            @Qualifier("jsonKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishCalendarEventDeleted(CalendarEventDeletedMessage message) {
        try {
            kafkaTemplate.send(deletedTopic, message.eventId().toString(), message.userIds()).get(3, TimeUnit.SECONDS);
            log.info("행사 취소 이벤트 발행 성공 : {}", message.eventId());
        } catch (Exception e) {
            log.error("행사 취소 이벤트 발행 실패 : {}", message.eventId());
            throw new CalendarException(CalendarErrorCode.INVALID_MESSAGE_PAYLOAD);
        }
    }

    public void publishCalendarEventUpdated(CalendarEventUpdatedMessage message) {
        try {
            kafkaTemplate.send(updatedTopic, message.eventId().toString(), message).get(3, TimeUnit.SECONDS);
            log.info("행사 변경 이벤트 발행 성공 : {}", message.eventId());
        } catch (Exception e) {
            log.error("행사 변경 이벤트 발행 실패 : {}", message.eventId());
            throw new CalendarException(CalendarErrorCode.INVALID_MESSAGE_PAYLOAD);
        }
    }
}
