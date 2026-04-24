package com.ojosama.operationservice.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BlacklistStatus {
    ACTIVE("정지 중"),
    INACTIVE("정지 해제됨");

    private final String description;
}
