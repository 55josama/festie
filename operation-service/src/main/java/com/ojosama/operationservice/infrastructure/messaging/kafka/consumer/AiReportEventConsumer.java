package com.ojosama.operationservice.infrastructure.messaging.kafka.consumer;

import com.ojosama.operationservice.application.dto.command.CreateReportCommand;
import com.ojosama.operationservice.application.service.ReportService;
import com.ojosama.operationservice.domain.event.payload.AiReportEvent;
import com.ojosama.operationservice.domain.model.enums.ReportCategory;
import com.ojosama.operationservice.domain.model.enums.ReportTargetType;
import com.ojosama.operationservice.domain.model.enums.ReporterType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiReportEventConsumer {
    private final ReportService reportService;

    // AI 시스템이 보낸 신고임을 식별하기 위한 고정 UUID
    private static final UUID AI_SYSTEM_ID = UUID.fromString("0");

    @KafkaListener(topics = "${spring.kafka.topic.ai-report}", groupId = "operation-service-group")
    public void consumeAiReport(AiReportEvent event) {

        CreateReportCommand command = new CreateReportCommand(
                AI_SYSTEM_ID,
                event.getTargetId(),
                event.getTargetUserId(),
                ReportTargetType.valueOf(event.getTargetType()),
                ReportCategory.valueOf(event.getCategory()),
                event.getDescription(),
                event.getContent()
        );

        // ReportService의 신고 로직 재사용
        // 유저 API 요청과 똑같이 저장 -> 3회 누적 체크 -> 블라인드 이벤트 발생
        reportService.createReport(command, ReporterType.SYSTEM_AI);
    }
}
