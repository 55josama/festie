package com.ojosama.chatbot.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.chatbot.application.service.DocumentIndexer;
import com.ojosama.chatbot.domain.event.payload.EventCreatedEvent;
import com.ojosama.chatbot.domain.event.payload.EventDeletedEvent;
import com.ojosama.chatbot.domain.event.payload.EventUpdatedEvent;
import com.ojosama.chatbot.infrastructure.client.EventClient;
import com.ojosama.chatbot.infrastructure.client.dto.EventClientResponse;
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
public class EventDocumentConsumer {
    private final DocumentIndexer documentIndexer;
    private final IdempotentEventHandler idempotentHandler;
    private final ObjectMapper objectMapper;
    private final EventClient eventClient;

    private static final String CONSUMER_GROUP = "ai-service-group";

    // 행사 생성 이벤트
    @KafkaListener(
            topics = "${spring.kafka.topic.event-created}",
            groupId = "ai-service-group"
    )
    public void consumeEventCreated(ConsumerRecord<String, String> record) {
        UUID messageKey;
        EventCreatedEvent event;

        try {
            messageKey = UUID.fromString(record.key());
            event = objectMapper.readValue(record.value(), EventCreatedEvent.class);
        } catch (Exception e) {
            log.error("[챗봇] 행사 생성 메시지 파싱 실패. key={}, value={}", record.key(), record.value(), e);
            return;
        }

        idempotentHandler.handle(
                messageKey,
                CONSUMER_GROUP,
                record.topic(),
                EventType.EVENT_CREATED.name(),
                () -> dispatchEventCreated(event)
        );
    }

    // 행사 삭제 이벤트
    @KafkaListener(
            topics = "${spring.kafka.topic.event-deleted}",
            groupId = "ai-service-group"
    )
    public void consumeEventDeleted(ConsumerRecord<String, String> record) {
        UUID messageKey;
        EventDeletedEvent event;

        try {
            messageKey = UUID.fromString(record.key());
            event = objectMapper.readValue(record.value(), EventDeletedEvent.class);
        } catch (Exception e) {
            log.error("[챗봇] 행사 삭제 메시지 파싱 실패. key={}, value={}", record.key(), record.value(), e);
            return;
        }

        idempotentHandler.handle(
                messageKey,
                CONSUMER_GROUP,
                record.topic(),
                EventType.EVENT_DELETED.name(),
                () -> dispatchEventDeleted(event)
        );
    }

    // 행사 일정 변경 이벤트
    @KafkaListener(
            topics = "${spring.kafka.topic.event-schedule-changed}",
            groupId = "ai-service-group"
    )
    public void consumeEventScheduleChanged(ConsumerRecord<String, String> record) {
        UUID messageKey;
        EventUpdatedEvent event;

        try {
            messageKey = UUID.fromString(record.key());
            event = objectMapper.readValue(record.value(), EventUpdatedEvent.class);
        } catch (Exception e) {
            log.error("[챗봇] 행사 일정 변경 메시지 파싱 실패. key={}, value={}", record.key(), record.value(), e);
            return;
        }

        idempotentHandler.handle(
                messageKey,
                CONSUMER_GROUP,
                record.topic(),
                EventType.EVENT_SCHEDULE_CHANGED.name(),
                () -> dispatchEventUpdated(event)
        );
    }

    private void dispatchEventCreated(EventCreatedEvent event) {
        log.info("[챗봇] 행사 생성 → 문서 인덱싱: eventId={}, eventName={}",
                event.eventId(), event.eventName());

        try {
            documentIndexer.indexEvent(
                    event.eventId(),
                    event.eventName(),
                    event.categoryName(),
                    event.eventStartAt(),
                    event.eventEndAt(),
                    event.place(),
                    event.latitude(),
                    event.longitude(),
                    event.minFee(),
                    event.maxFee(),
                    event.hasTicketing(),
                    event.ticketingOpenAt(),
                    event.ticketingCloseAt(),
                    event.ticketingLink(),
                    event.status(),
                    event.officialLink(),
                    event.description(),
                    event.performer(),
                    event.img()
            );

            log.info("[챗봇] 행사 문서 인덱싱 완료: eventId={}", event.eventId());
        } catch (Exception e) {
            log.error("[챗봇] 행사 문서 인덱싱 실패: eventId={}, eventName={}",
                    event.eventId(), event.eventName(), e);
            throw e;
        }
    }

    private void dispatchEventDeleted(EventDeletedEvent event) {
        log.info("[챗봇] 행사 삭제 → 문서 제거: eventId={}, eventName={}",
                event.eventId(), event.eventName());

        try {
            documentIndexer.deleteEvent(event.eventId());
            log.info("[챗봇] 행사 문서 제거 완료: eventId={}", event.eventId());
        } catch (Exception e) {
            log.error("[챗봇] 행사 문서 제거 실패: eventId={}, eventName={}",
                    event.eventId(), event.eventName(), e);
            throw e;
        }
    }

    private void dispatchEventUpdated(EventUpdatedEvent event) {
        log.info("[챗봇] 행사 일정 변경 → 문서 재인덱싱: eventId={}, eventName={}, 변경 필드 수={}",
                event.eventId(), event.eventName(),
                event.changedFields() != null ? event.changedFields().size() : 0);

        // 변경된 내용으로 문서 재인덱싱 (전체 업데이트)
        try {
            log.debug("[챗봇] event-service에서 행사 전체 정보 조회 시작: eventId={}", event.eventId());

            EventClientResponse fullEvent = eventClient.getEventById(event.eventId());

            if (fullEvent != null) {
                log.debug("[챗봇] 행사 전체 정보 조회 성공: eventId={}, categoryName={}",
                        event.eventId(), fullEvent.categoryName());

                documentIndexer.indexEvent(
                        fullEvent.id(),
                        fullEvent.name(),
                        fullEvent.categoryName(),      // event-service에서 조회한 값
                        fullEvent.startAt(),           // event-service에서 조회한 값
                        fullEvent.endAt(),             // event-service에서 조회한 값
                        fullEvent.place(),
                        fullEvent.latitude(),
                        fullEvent.longitude(),
                        fullEvent.minFee(),
                        fullEvent.maxFee(),
                        fullEvent.hasTicketing(),
                        fullEvent.ticketingOpenAt(),
                        fullEvent.ticketingCloseAt(),
                        fullEvent.ticketingLink(),
                        fullEvent.status(),
                        fullEvent.officialLink(),
                        fullEvent.description(),
                        fullEvent.performer(),
                        fullEvent.img()
                );

                log.info("[챗봇] 행사 문서 재인덱싱 완료: eventId={}", event.eventId());
            } else {
                log.warn("[챗봇] event-service에서 행사 정보를 찾을 수 없음: eventId={}", event.eventId());
                // 조회 실패 시 변경 이벤트의 정보만으로 업데이트
                documentIndexer.indexEvent(
                        event.eventId(),
                        event.eventName(),
                        null,  // categoryName 누락 → "카테고리 미정"
                        null,  // startAt 누락 → "미정"
                        null,  // endAt 누락 → "미정"
                        event.place(),
                        event.latitude(),
                        event.longitude(),
                        event.minFee(),
                        event.maxFee(),
                        event.hasTicketing(),
                        event.ticketingOpenAt(),
                        event.ticketingCloseAt(),
                        event.ticketingLink(),
                        event.status(),
                        event.officialLink(),
                        event.description(),
                        event.performer(),
                        event.img()
                );
                log.warn("[챗봇] 행사 문서 재인덱싱 완료 (일부 필드 누락): eventId={}", event.eventId());
            }
        } catch (Exception e) {
            log.error("[챗봇] 행사 문서 재인덱싱 실패: eventId={}, eventName={}",
                    event.eventId(), event.eventName(), e);
            throw e;
        }
    }
}
