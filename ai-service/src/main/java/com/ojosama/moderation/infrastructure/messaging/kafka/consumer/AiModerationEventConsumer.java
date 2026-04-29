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

        List<AiModerationRequestEvent> eventsToProcess = new ArrayList<>();
        List<Runnable> inboxConfirmTasks = new ArrayList<>();

        for (ConsumerRecord<String, String> record : records) {
            UUID messageKey;
            AiModerationRequestEvent event;

            try {
                messageKey = UUID.fromString(record.key());
                event = objectMapper.readValue(record.value(), AiModerationRequestEvent.class);
            } catch (Exception e) {
                log.error("메시지 파싱 실패. topic={}, partition={}, offset={}, key={}, payloadLength={}",
                        record.topic(), record.partition(), record.offset(), record.key(),
                        record.value() == null ? 0 : record.value().length(), e);
                continue;
            }

            eventsToProcess.add(event);

            // Inbox 확정 로직을 지금 실행하지 않고 Runnable로 담아두기만 함
            inboxConfirmTasks.add(() -> {
                try {
                    idempotentHandler.handle(messageKey, CONSUMER_GROUP, record.topic(), eventType, () -> {
                        // 비즈니스 로직은 일괄 처리로 끝났으므로, 여기서는 Inbox 상태만 '완료'로 기록
                    });
                } catch (Exception e) {
                    log.error("Inbox 상태 마킹 실패: key={}, error={}", messageKey, e.getMessage());
                }
            });
        }

        if (!eventsToProcess.isEmpty()) {
            // 비즈니스 로직(AI 모델 일괄 호출)을 먼저 실행
            // 여기서 에러가 발생해 던져지면, 아래의 Inbox 마킹(task.run)은 실행되지 않아 유실을 방지
            log.info("[AI 검증] {}개의 검사 요청 배치를 서비스로 전달합니다. (Type: {})", eventsToProcess.size(), eventType);
            aiModerationService.processModerationBatch(eventsToProcess);

            // 비즈니스 로직이 무사히 성공한 이후에만 모아둔 Inbox 마킹 작업 확정
            for (Runnable task : inboxConfirmTasks) {
                task.run();
            }
        }
    }
}
