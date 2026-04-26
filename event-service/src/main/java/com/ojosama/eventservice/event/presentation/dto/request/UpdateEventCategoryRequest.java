package com.ojosama.eventservice.event.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateEventCategoryRequest(
        @NotBlank(message = "카테고리 이름은 필수입니다.")
        String name
) {}
