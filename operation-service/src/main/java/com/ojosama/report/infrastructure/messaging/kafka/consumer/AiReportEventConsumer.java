package com.ojosama.report.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import com.ojosama.report.application.dto.command.CreateReportCommand;
import com.ojosama.report.application.service.ReportService;
import com.ojosama.report.domain.event.payload.AiReportEvent;
import com.ojosama.report.domain.model.enums.ReportCategory;
import com.ojosama.report.domain.model.enums.ReportTargetType;
import com.ojosama.report.domain.model.enums.ReporterType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiReportEventConsumer {
    private final ReportService reportService;
    private final IdempotentEventHandler idempotentHandler;
    private final ObjectMapper objectMapper;

    // CONSUMER_GROUP, EVENT_TYPE 상수 선언
    private static final String CONSUMER_GROUP = "operation-service-group";
    private static final String EVENT_TYPE = EventType.AI_MODERATION_EVALUATED.name();

    // AI 시스템이 보낸 신고임을 식별하기 위한 고정 UUID
    private static final UUID AI_SYSTEM_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @KafkaListener(topics = "${spring.kafka.topic.ai-moderation-reported}", groupId = "operation-service-group")
    public void consumeAiReport(ConsumerRecord<String, String> record) {
        UUID messageKey;
        AiReportEvent event;

        try {
            messageKey = UUID.fromString(record.key());
            event = objectMapper.readValue(record.value(), AiReportEvent.class);
        } catch (Exception e) {
            log.error("AI Report 메시지 파싱 실패. key={}, value={}", record.key(), record.value(), e);
            return;
        }

        // Inbox 패턴의 핵심: 멱등성 핸들러 내부에서 비즈니스 로직 호출
        idempotentHandler.handle(
                messageKey,
                CONSUMER_GROUP,
                record.topic(),
                EVENT_TYPE,
                () -> dispatch(event)
        );
    }

    private void dispatch(AiReportEvent event){
        try {
            if (event == null || event.targetType() == null || event.category() == null) {
                log.error("AI 신고 이벤트 필수 필드 누락. 처리를 스킵합니다. event: {}", event);
                return;
            }

            // 대소문자 무시 및 공백 제거 후 파싱, 실패 시 예외 발생
            ReportTargetType targetType = ReportTargetType.valueOf(event.targetType().toUpperCase().trim());
            ReportCategory category = ReportCategory.valueOf(event.category().toUpperCase().trim());

            CreateReportCommand command = new CreateReportCommand(
                    AI_SYSTEM_ID,
                    event.targetId(),
                    event.targetUserId(),
                    targetType,
                    category,
                    "AI 자동 모더레이션 시스템에 의한 유해 콘텐츠 신고입니다.",
                    event.content()
            );

            reportService.createReport(command, ReporterType.SYSTEM_AI);

        } catch (IllegalArgumentException e) {
            // Enum 파싱 실패 시 무한 루프 방지를 위해 에러 로그만 남기고 이벤트 스킵
            log.error("AI 신고 이벤트 데이터 형식이 올바르지 않습니다. 처리를 스킵합니다. event: {}", event, e);
        }
    }
}
