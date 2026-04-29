package com.ojosama.moderation.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import com.ojosama.moderation.application.service.AiModerationService;
import com.ojosama.moderation.domain.event.payload.AiModerationRequestEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiModerationEventConsumer {
    private final AiModerationService aiModerationService;
    private final IdempotentEventHandler idempotentHandler;
    private final ObjectMapper objectMapper;

    private static final String CONSUMER_GROUP = "ai-service-group";
    private static final String EVENT_TYPE_CHAT_REQUEST = EventType.CHAT_MODERATION_REQUESTED.name();
    private static final String EVENT_TYPE_COMMUNITY_REQUEST = EventType.COMMUNITY_MODERATION_REQUESTED.name();

    // 채팅용 리스너 (chatBatchFactory 사용)
    @KafkaListener(
            topics = "${spring.kafka.topic.chat-moderation-requested}",
            groupId = "ai-service-group",
            containerFactory = "chatBatchFactory"
    )
    public void consumeChatBatch(List<ConsumerRecord<String, String>> records) {
        log.info("[AI 검증] 채팅 검사 요청 배치 수신. size={}", records.size());
        processRecordsInBatch(records, EVENT_TYPE_CHAT_REQUEST);
    }

    // 커뮤니티용 리스너 (communityBatchFactory 사용)
    @KafkaListener(
            topics = "${spring.kafka.topic.community-moderation-requested}",
            groupId = "ai-service-group",
            containerFactory = "communityBatchFactory"
    )
    public void consumeCommunityBatch(List<ConsumerRecord<String, String>> records) {
        log.info("[AI 검증] 커뮤니티 검사 요청 배치 수신. size={}", records.size());
        processRecordsInBatch(records, EVENT_TYPE_COMMUNITY_REQUEST);
    }

    private void processRecordsInBatch(List<ConsumerRecord<String, String>> records, String eventType) {
        if (records == null || records.isEmpty()) {
            return;
        }

        List<AiModerationRequestEvent> validEvents = new ArrayList<>();

        for (ConsumerRecord<String, String> record : records) {
            UUID messageKey;
            AiModerationRequestEvent event;

            try {
                messageKey = UUID.fromString(record.key());
                event = objectMapper.readValue(record.value(), AiModerationRequestEvent.class);
            } catch (Exception e) {
                log.error("메시지 파싱 실패. key={}, value={}", record.key(), record.value(), e);
                continue;
            }

            try {
                // 전달받은 eventType을 사용하여 멱등성 검증
                idempotentHandler.handle(
                        messageKey, CONSUMER_GROUP, record.topic(), eventType,
                        () -> {
                            validEvents.add(event);
                        }
                );
            } catch (Exception e) {
                log.error("멱등성 처리 또는 이벤트 담기 실패: {}", messageKey, e);
            }
        }

        if (!validEvents.isEmpty()) {
            log.info("[AI 검증] 중복 제외 {}개의 유효한 검사 요청 배치를 서비스로 전달합니다. (Type: {})", validEvents.size(), eventType);
            aiModerationService.processModerationBatch(validEvents);
        }
    }
}
