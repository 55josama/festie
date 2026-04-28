package com.ojosama.moderation.application.service;

import com.ojosama.moderation.domain.event.AiModerationEventProducer;
import com.ojosama.moderation.domain.event.payload.AiModerationRequestEvent;
import com.ojosama.moderation.domain.model.entity.AiModeration;
import com.ojosama.moderation.domain.model.enums.ReportCategory;
import com.ojosama.moderation.domain.repository.AiModerationRepository;
import com.ojosama.moderation.infrastructure.client.AiModerationClient;
import com.ojosama.moderation.infrastructure.client.dto.AiModerationClientResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiModerationService {
    private final AiModerationClient aiModerationClient;
    private final AiModerationRepository aiModerationRepository;
    private final AiModerationEventProducer eventProducer;

    @Transactional
    public void processModerationBatch(List<AiModerationRequestEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        // AI 환각(Hallucination) 방어를 위한 원본 이벤트 Map 생성
        Map<UUID, AiModerationRequestEvent> eventMap = events.stream()
                .collect(Collectors.toMap(AiModerationRequestEvent::targetId, e -> e));

        // AI 모델 일괄 호출
        List<AiModerationClientResponse> aiResponses = aiModerationClient.analyzeBatch(events);

        // AI 응답과 원본 데이터를 결합하여 엔티티 조립
        List<AiModeration> moderation = aiResponses.stream()
                .map(response -> {
                    AiModerationRequestEvent origin = eventMap.get(response.targetId());

                    // AI 환각 방어: 전송하지 않은 ID를 반환한 경우
                    if (origin == null) {
                        log.warn("존재하지 않는 targetId 반환: {}", response.targetId());
                        return null;
                    }

                    return AiModeration.of(
                            origin.targetId(),
                            origin.targetUserId(),
                            origin.targetType(),
                            response.category(),
                            origin.content()
                    );
                })
                .filter(Objects::nonNull)
                .toList();

        // saveAll로 일괄 저장
        List<AiModeration> savedModeration = aiModerationRepository.saveAll(moderation);

        // 유해 콘텐츠(SAFE가 아닌 것)만 필터링하여 운영 서버로 알림 전송
        savedModeration.stream()
                .filter(m -> m.getCategory() != ReportCategory.SAFE)
                .forEach(eventProducer::publishEvaluatedEvent);
    }
}
