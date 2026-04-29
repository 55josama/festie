package com.ojosama.chatbot.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.chatbot.application.service.DocumentIndexer;
import com.ojosama.chatbot.domain.event.payload.EventsUpdateEvent;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventsUpdateEventConsumer {
    private final DocumentIndexer documentIndexer;
    private final IdempotentEventHandler idempotentHandler;
    private final ObjectMapper objectMapper;

    private static final String CONSUMER_GROUP = "ai-service-group";
    private static final String EVENT_TYPE = EventType.EVENT_EVENT_UPDATED.name();

    @KafkaListener(topics = "event-event-updated", groupId = "ai-service-group")
    public void consumeEvent(ConsumerRecord<String, String> record) {
        UUID messageKey;
        EventsUpdateEvent event;

        try {
            // 메시지 키 파싱 및 이벤트 역직렬화
            messageKey = UUID.fromString(record.key());
            event = objectMapper.readValue(record.value(), EventsUpdateEvent.class);
        } catch (Exception e) {
            log.error("Event 메시지 파싱 실패. key={}, value={}", record.key(), record.value(), e);
            return;
        }
        idempotentHandler.handle(
                messageKey,
                CONSUMER_GROUP,
                record.topic(),
                EVENT_TYPE,
                () -> dispatch(event)
        );
    }

    private void dispatch(EventsUpdateEvent message) {
        if ("CANCELLED".equalsIgnoreCase(message.status())) {
            log.info("Kafka: 행사 취소 처리 (EventID: {})", message.eventId());
            documentIndexer.deleteEvent(message.eventId());
            return;
        }

        log.info("Kafka: 행사 업데이트/생성 Upsert (EventID: {})", message.eventId());
        documentIndexer.indexEvent(
                message.eventId(),
                message.name(),
                message.categoryName(),
                message.startAt() != null ? message.startAt().toString() : "미정",
                message.endAt() != null ? message.endAt().toString() : "미정",
                message.place(),
                message.hasTicketing(),
                message.officialLink(),
                message.description(),
                message.performer()
        );
    }
}
