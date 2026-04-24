package com.ojosama.operationservice.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OperationRequestStatus {
    PENDING("대기 중"),
    IN_PROGRESS("진행 중"),
    RESOLVED("반영됨"),
    REJECTED("반려됨");

    private final String description;
}
