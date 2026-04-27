package com.ojosama.operationservice.infrastructure.messaging.kafka.consumer;

import com.ojosama.operationservice.application.dto.command.CreateReportCommand;
import com.ojosama.operationservice.application.service.ReportService;
import com.ojosama.operationservice.domain.event.payload.AiReportEvent;
import com.ojosama.operationservice.domain.model.enums.ReportCategory;
import com.ojosama.operationservice.domain.model.enums.ReportTargetType;
import com.ojosama.operationservice.domain.model.enums.ReporterType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiReportEventConsumer {
    private final ReportService reportService;

    // AI 시스템이 보낸 신고임을 식별하기 위한 고정 UUID
    private static final UUID AI_SYSTEM_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @KafkaListener(topics = "${spring.kafka.topic.ai-report}", groupId = "operation-service-group")
    public void consumeAiReport(AiReportEvent event) {
        try {
            // 대소문자 무시 및 공백 제거 후 파싱, 실패 시 예외 발생
            ReportTargetType targetType = ReportTargetType.valueOf(event.getTargetType().toUpperCase().trim());
            ReportCategory category = ReportCategory.valueOf(event.getCategory().toUpperCase().trim());

            CreateReportCommand command = new CreateReportCommand(
                    AI_SYSTEM_ID,
                    event.getTargetId(),
                    event.getTargetUserId(),
                    targetType,
                    category,
                    "AI 자동 모더레이션 시스템에 의한 유해 콘텐츠 신고입니다.",
                    event.getContent()
            );

            reportService.createReport(command, ReporterType.SYSTEM_AI);

        } catch (IllegalArgumentException e) {
            // Enum 파싱 실패 시 무한 루프 방지를 위해 에러 로그만 남기고 이벤트 스킵
            log.error("AI 신고 이벤트 데이터 형식이 올바르지 않습니다. 처리를 스킵합니다. event: {}", event, e);
        }
    }
}
