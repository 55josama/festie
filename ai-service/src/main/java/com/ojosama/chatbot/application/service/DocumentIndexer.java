package com.ojosama.chatbot.application.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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
    public void indexEvent(UUID eventId, String name, String categoryName,
                           LocalDateTime startAt, LocalDateTime endAt,
                           String place, BigDecimal latitude, BigDecimal longitude,
                           Integer minFee, Integer maxFee,
                           Boolean hasTicketing, LocalDateTime ticketingOpenAt,
                           LocalDateTime ticketingCloseAt, String ticketingLink,
                           String status, String officialLink,
                           String description, String performer, String img) {

        // null 안전 처리
        String safeStatus = (status == null || status.isBlank()) ? "UNKNOWN" : status;
        String safePlace = (place != null && !place.isBlank()) ? place : "장소 미정";
        String safePerformer = (performer != null && !performer.isBlank()) ? performer : "정보 없음";
        String safeLink = (officialLink != null && !officialLink.isBlank()) ? officialLink : "없음";
        String safeDescription = (description != null && !description.isBlank()) ? description : "설명 없음";
        String safeImg = (img != null && !img.isBlank()) ? img : "이미지 없음";

        // 상태값을 한글로 매핑
        String statusKor = switch (safeStatus.toUpperCase()) {
            case "SCHEDULED" -> "예정됨";
            case "IN_PROGRESS" -> "진행 중";
            case "COMPLETED" -> "완료됨 (종료)";
            case "CANCELLED" -> "취소됨";
            default -> status;
        };

        // 티켓팅 정보 포맷팅
        String ticketingInfo;
        if (hasTicketing != null && hasTicketing) {
            StringBuilder ticketing = new StringBuilder("필요");
            if (ticketingOpenAt != null && ticketingCloseAt != null) {
                ticketing.append(String.format(" (예매기간: %s ~ %s)",
                        ticketingOpenAt, ticketingCloseAt));
            }
            if (ticketingLink != null && !ticketingLink.isBlank()) {
                ticketing.append(String.format(", 예매링크: %s", ticketingLink));
            }
            ticketingInfo = ticketing.toString();
        } else {
            ticketingInfo = "불필요/미정";
        }

        // 요금 정보 포맷팅
        String feeInfo;
        if (minFee != null && maxFee != null) {
            if (minFee.equals(maxFee)) {
                feeInfo = String.format("%,d원", minFee);
            } else {
                feeInfo = String.format("%,d원 ~ %,d원", minFee, maxFee);
            }
        } else if (minFee != null) {
            feeInfo = String.format("%,d원부터", minFee);
        } else if (maxFee != null) {
            feeInfo = String.format("최대 %,d원", maxFee);
        } else {
            feeInfo = "무료/미정";
        }

        // 위치 정보 포맷팅
        String locationInfo = safePlace;
        if (latitude != null && longitude != null) {
            locationInfo += String.format(" (위도: %s, 경도: %s)", latitude, longitude);
        }

        // 기간 포맷팅
        String periodInfo;
        if (startAt != null && endAt != null) {
            periodInfo = String.format("%s ~ %s", startAt, endAt);
        } else if (startAt != null) {
            periodInfo = String.format("%s 시작", startAt);
        } else {
            periodInfo = "미정";
        }

        String content = String.format(
                "[행사정보]\n" +
                        "행사명: %s\n" +
                        "상태: %s\n" +
                        "카테고리: %s\n" +
                        "기간: %s\n" +
                        "위치: %s\n" +
                        "출연진/공연자: %s\n" +
                        "입장료: %s\n" +
                        "티켓팅: %s\n" +
                        "공식링크: %s\n" +
                        "설명: %s\n" +
                        "이미지: %s\n" +
                        "상세링크: %s",
                name, statusKor, categoryName, periodInfo, locationInfo,
                safePerformer, feeInfo, ticketingInfo, safeLink,
                safeDescription, safeImg, eventId.toString()
        );

        Document doc = new Document(eventId.toString(), content, Map.of(
                "docType", "EVENT",
                "eventId", eventId.toString(),
                "status", safeStatus,
                "categoryName", categoryName,
                "place", safePlace
        ));

        vectorStore.add(List.of(doc));
    }

    // 행사 삭제/취소 시 VectorStore에서 문서 완전히 제거
    public void deleteEvent(UUID eventId) {
        vectorStore.delete(List.of(eventId.toString()));
    }
}
