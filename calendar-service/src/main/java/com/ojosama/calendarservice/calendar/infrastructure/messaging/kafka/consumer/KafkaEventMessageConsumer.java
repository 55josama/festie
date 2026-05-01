package com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.calendarservice.calendar.application.CalendarCommandService;
import com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.dto.EventDeletedMessage;
import com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.dto.EventScheduleChangedMessage;
import com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.dto.FieldChange;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventMessageConsumer {

    private final CalendarCommandService calendarCommandService;
    private final IdempotentEventHandler idempotentHandler;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.consumer.group-id}")
    private String consumerGroup;

    @KafkaListener(
            topics = "${spring.kafka.topic.event-deleted}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleEventDeleted(ConsumerRecord<String, String> record) {
        UUID messageKey = UUID.fromString(record.key());
        idempotentHandler.handle(messageKey, consumerGroup, record.topic(),
                EventType.EVENT_DELETED.getValue(), () -> {
                    try {
                        EventDeletedMessage msg = objectMapper.readValue(record.value(), EventDeletedMessage.class);
                        calendarCommandService.deleteAllByEventId(msg.eventId());
                        log.info("[Kafka] 행사 삭제 처리 완료: eventId={}", msg.eventId());
                    } catch (Exception e) {
                        throw new RuntimeException("[Kafka] 행사 삭제 처리 실패: key=" + messageKey, e);
                    }
                });
    }

    @KafkaListener(
            topics = "${spring.kafka.topic.event-changed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleEventScheduleChanged(ConsumerRecord<String, String> record) {
        UUID messageKey = UUID.fromString(record.key());
        idempotentHandler.handle(messageKey, consumerGroup, record.topic(),
                EventType.EVENT_SCHEDULE_CHANGED.getValue(), () -> {
                    try {
                        EventScheduleChangedMessage msg = objectMapper.readValue(
                                record.value(), EventScheduleChangedMessage.class);
                        List<FieldChange> changedFields = msg.changedFields();

                        LocalDateTime newDate = extractLocalDateTime(changedFields, "startAt");
                        String newName = extractString(changedFields, "name");
                        LocalDateTime newTicketingDate = extractLocalDateTime(changedFields, "ticketingOpenAt");

                        calendarCommandService.updateEventInfo(msg.eventId(), newDate, newName, newTicketingDate);
                        log.info("[Kafka] 행사 정보 변경 처리 완료: eventId={}", msg.eventId());
                    } catch (Exception e) {
                        throw new RuntimeException("[Kafka] 행사 정보 변경 처리 실패: key=" + messageKey, e);
                    }
                });
    }

    private LocalDateTime extractLocalDateTime(List<FieldChange> changedFields, String fieldName) {
        if (changedFields == null) return null;
        return changedFields.stream()
                .filter(f -> fieldName.equals(f.fieldName()) && f.after() != null)
                .findFirst()
                .map(f -> objectMapper.convertValue(f.after(), LocalDateTime.class))
                .orElse(null);
    }

    private String extractString(List<FieldChange> changedFields, String fieldName) {
        if (changedFields == null) return null;
        return changedFields.stream()
                .filter(f -> fieldName.equals(f.fieldName()) && f.after() != null)
                .findFirst()
                .map(f -> f.after().toString())
                .orElse(null);
    }
}
