package com.ojosama.chatbot.application.service;

import java.nio.charset.StandardCharsets;
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
        String validUuid = UUID.nameUUIDFromBytes(guideId.getBytes(StandardCharsets.UTF_8)).toString();

        Document doc = new Document(validUuid, content, Map.of(
                "docType", "GUIDE",
                "guideId", guideId // 메타데이터에 원래 이름 남겨두기
        ));

        vectorStore.add(List.of(doc));
    }

    // 행사 정보 인덱싱
    public void indexEvent(UUID eventId, String name, String categoryName, String startAt, String endAt,
                           String place, Boolean hasTicketing, String officialLink,
                           String description, String performer, String status) {

        // AI가 문맥을 잘 이해할 수 있도록 null / 빈 값 안전 처리
        String safeStatus = (status == null || status.isBlank()) ? "UNKNOWN" : status;
        String ticketingInfo = (hasTicketing != null && hasTicketing) ? "필요" : "불필요/미정";
        String safePerformer = (performer != null && !performer.isBlank()) ? performer : "정보 없음";
        String safeLink = (officialLink != null && !officialLink.isBlank()) ? officialLink : "없음";

        // 상태값을 한글로 매핑하여 AI가 더 잘 이해하게 돕기
        String statusKor = switch (safeStatus.toUpperCase()) {
            case "SCHEDULED" -> "예정됨";
            case "IN_PROGRESS" -> "진행 중";
            case "COMPLETED" -> "완료됨 (종료)";
            case "CANCELLED" -> "취소됨";
            default -> status;
        };

        String content = String.format(
                "[행사정보]\n행사명: %s\n상태: %s\n카테고리: %s\n기간: %s ~ %s\n위치: %s\n출연진: %s\n티켓팅: %s\n공식링크: %s\n설명: %s\n상세링크: %s",
                name, statusKor, categoryName, startAt, endAt, place, safePerformer, ticketingInfo, safeLink, description, eventId.toString()
        );

        Document doc = new Document(eventId.toString(), content, Map.of(
                "docType", "EVENT",
                "eventId", eventId.toString(),
                "status", safeStatus
        ));

        vectorStore.add(List.of(doc));
    }

    // 행사 삭제/취소 시 VectorStore에서 문서 완전히 제거
    public void deleteEvent(UUID eventId) {
        vectorStore.delete(List.of(eventId.toString()));
    }
}
