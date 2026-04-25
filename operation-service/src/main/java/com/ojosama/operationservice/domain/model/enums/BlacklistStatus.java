package com.ojosama.operationservice.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BlacklistStatus {
    ACTIVE("차단 중"),
    INACTIVE("차단 해제됨");

    private final String description;
}
