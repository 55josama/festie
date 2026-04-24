package com.ojosama.operationservice.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReporterType {
    USER("일반 사용자"),
    SYSTEM_AI("AI 시스템");

    private final String description;
}
