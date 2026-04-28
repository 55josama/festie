package com.ojosama.eventservice.event.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UpdateEventRequest(
        @NotBlank(message = "행사 이름은 필수입니다.")
        String name,
        @NotNull(message = "카테고리 ID는 필수입니다.")
        UUID categoryId,
        @NotNull(message = "시작 시간은 필수입니다.")
        LocalDateTime startAt,
        @NotNull(message = "종료 시간은 필수입니다.")
        LocalDateTime endAt,
        @NotBlank(message = "장소는 필수입니다.")
        String place,
        @NotNull(message = "위도는 필수입니다.")
        BigDecimal latitude,
        @NotNull(message = "경도는 필수입니다.")
        BigDecimal longitude,
        @NotNull(message = "최소 금액은 필수입니다.")
        Integer minFee,
        @NotNull(message = "최대 금액은 필수입니다.")
        Integer maxFee,
        @NotNull(message = "티켓팅 여부는 필수입니다.")
        Boolean hasTicketing,
        LocalDateTime ticketingOpenAt,
        LocalDateTime ticketingCloseAt,
        String ticketingLink,
        String officialLink,
        @NotBlank(message = "설명은 필수입니다.")
        String description,
        String performer,
        @NotBlank(message = "이미지 URL은 필수입니다.")
        String img,
        @Valid
        List<CreateScheduleRequest> schedules
) {
}
