package com.ojosama.chatbot.application.init;

import com.ojosama.chatbot.application.service.DocumentIndexer;
import com.ojosama.chatbot.infrastructure.client.EventClient;
import com.ojosama.chatbot.infrastructure.client.dto.EventClientResponse;
import com.ojosama.common.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitialDocumentSaver {
    private final DocumentIndexer documentIndexer;
    private final EventClient eventClient;

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void loadInitialData() {
        log.info("Chatbot Service: 초기 지식 데이터 동기화 시작...");

        loadSystemGuides();

        try {
            ApiResponse<List<EventClientResponse>> response = eventClient.getEvents();

            if (response != null && response.getData() != null) {
                List<EventClientResponse> events = response.getData();

                for (EventClientResponse event : events) {
                    documentIndexer.indexEvent(
                            event.id(),
                            event.name(),
                            event.categoryName(),
                            event.startAt() != null ? event.startAt().toString() : "미정",
                            event.endAt() != null ? event.endAt().toString() : "미정",
                            event.place(),
                            event.hasTicketing(),
                            event.officialLink(),
                            event.description(),
                            event.performer(),
                            event.status()
                    );
                }
                log.info("Chatbot Service: 행사 데이터 {}건 동기화 완료", events.size());
            }
        } catch (Exception e) {
            log.error("Chatbot Service: 데이터 초기 동기화 중 오류 발생", e);
        }
    }

    private void loadSystemGuides() {
        documentIndexer.indexGuide("chat_guide", "행사별 실시간 채팅방은 행사 시작 시간에 맞춰 자동 오픈됩니다.");
        documentIndexer.indexGuide("report_guide", "악성 유저 신고 시 자동 블라인드 및 누적 5회 시 계정 정지 정책을 운영 중입니다.");
    }
}
