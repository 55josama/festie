package com.ojosama.eventservice.event.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateScheduleRequest(
    @NotBlank(message = "일정 이름은 필수입니다.")
    String name,
    @NotNull(message = "일정 시작 시간은 필수입니다.")
    LocalDateTime startTime,
    @NotNull(message = "일정 종료 시간은 필수입니다.")
    LocalDateTime endTime
) {}
