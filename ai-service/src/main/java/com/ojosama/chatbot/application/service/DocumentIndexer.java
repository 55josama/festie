package com.ojosama.chatbot.application.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentIndexer {
    private final VectorStore vectorStore;

    // 일반 정책/가이드 텍스트 인덱싱
    public void indexGuide(String guideId, String content) {
        Document doc = new Document("guide_" + guideId, content, Map.of("docType", "GUIDE"));
        vectorStore.add(List.of(doc));
    }

    // 행사 정보 인덱싱
    public void indexEvent(UUID eventId, String name, String categoryName, String startAt, String endAt,
                           String place, Boolean hasTicketing, String officialLink,
                           String description, String performer) {

        // AI가 문맥을 잘 이해할 수 있도록 null / 빈 값 안전 처리
        String ticketingInfo = (hasTicketing != null && hasTicketing) ? "필요" : "불필요/미정";
        String safePerformer = (performer != null && !performer.isBlank()) ? performer : "정보 없음";
        String safeLink = (officialLink != null && !officialLink.isBlank()) ? officialLink : "없음";

        String content = String.format(
                "[행사정보] 행사명: %s, 카테고리: %s, 기간: %s ~ %s, 위치: %s, 출연진: %s, 티켓팅: %s, 공식링크: %s, 설명: %s, 상세링크: %s",
                name, categoryName, startAt, endAt, place, safePerformer, ticketingInfo, safeLink, description, eventId.toString()
        );

        Document doc = new Document("event_" + eventId.toString(), content, Map.of(
                "docType", "EVENT",
                "eventId", eventId.toString()
        ));

        vectorStore.add(List.of(doc));
    }

    // 3. 행사 삭제/취소 시 VectorStore에서 문서 완전히 제거
    public void deleteEvent(UUID eventId) {
        vectorStore.delete(List.of("event_" + eventId.toString()));
    }
}
