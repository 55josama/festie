package com.ojosama.chatbot.infrastructure.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventClientResponse(
        UUID id,
        String name,
        UUID categoryId,
        String categoryName,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String place,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer minFee,
        Integer maxFee,
        Boolean hasTicketing,
        LocalDateTime ticketingOpenAt,
        LocalDateTime ticketingCloseAt,
        String ticketingLink,
        String officialLink,
        String description,
        String performer,
        String img,
        String status,
        List<ScheduleResponse> schedules  // 추가된 필드
) {
    // schedules가 null이면 빈 리스트 반환하는 헬퍼 메서드
    public List<ScheduleResponse> safeSchedules() {
        return schedules != null ? schedules : List.of();
    }

    public record ScheduleResponse(
            UUID id,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String description
    ) { }
}
