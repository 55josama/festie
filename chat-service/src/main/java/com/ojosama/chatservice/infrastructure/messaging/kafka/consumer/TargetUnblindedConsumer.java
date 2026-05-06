package com.ojosama.chatservice.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.chatservice.application.service.MessageService;
import com.ojosama.chatservice.domain.exception.ChatException;
import com.ojosama.chatservice.infrastructure.messaging.kafka.dto.TargetUnblindedEvent;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetUnblindedConsumer {

    private static final String CONSUMER_GROUP = "chat-service-group";
    private static final String EVENT_TYPE = EventType.REPORT_UNBLINDED.getValue();
    private static final String TARGET_TYPE_CHAT = "CHAT";

    private final ObjectMapper objectMapper;
    private final IdempotentEventHandler idempotentHandler;
    private final MessageService messageService;

    @KafkaListener(
            topics = "${spring.kafka.topic.report-unblinded}",
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        UUID messageKey;
        TargetUnblindedEvent event;

        try {
            messageKey = UUID.fromString(record.key());
            event = parse(record.value());
            idempotentHandler.handle(
                    messageKey,
                    CONSUMER_GROUP,
                    record.topic(),
                    EVENT_TYPE,
                    () -> dispatch(event)
            );
            log.info("블라인드 해제 이벤트 처리를 완료했습니다. key={}, topic={}, targetType={}, targetId={}, reason={}",
                    record.key(), record.topic(), event.targetType(), event.targetId(), event.reason());
        } catch (RuntimeException e) {
            log.error("블라인드 해제 이벤트 처리에 실패했습니다. key={}, payload={}",
                    record.key(), record.value(), e);
            throw e;
        }
    }

    private void dispatch(TargetUnblindedEvent event) {
        if (event.targetId() == null || event.targetType() == null) {
            log.warn("블라인드 해제 이벤트 필수 필드 누락으로 스킵합니다. event={}", event);
            return;
        }

        String normalizedTargetType = event.targetType().trim().toUpperCase(Locale.ROOT);
        if (!TARGET_TYPE_CHAT.equals(normalizedTargetType)) {
            log.debug("chat-service 대상이 아닌 블라인드 해제 이벤트라 스킵합니다. targetType={}, targetId={}",
                    event.targetType(), event.targetId());
            return;
        }

        try {
            messageService.unblindMessageBySystem(event.targetId());
            if (event.reason() != null && !event.reason().isBlank()) {
                log.info("메시지 블라인드 해제 완료. targetId={}, reason={}", event.targetId(), event.reason());
            } else {
                log.info("메시지 블라인드 해제 완료. targetId={}", event.targetId());
            }
        } catch (ChatException e) {
            if (HttpStatus.NOT_FOUND.equals(e.getStatus()) || HttpStatus.BAD_REQUEST.equals(e.getStatus())) {
                log.warn("블라인드 해제 대상 메시지를 적용할 수 없어 스킵합니다. targetId={}, status={}, message={}",
                        event.targetId(), e.getStatus(), e.getMessage());
                return;
            }
            throw e;
        }
    }

    private TargetUnblindedEvent parse(String payload) {
        try {
            return objectMapper.readValue(payload, TargetUnblindedEvent.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("블라인드 해제 이벤트 페이로드 파싱에 실패했습니다.", e);
        }
    }
}
