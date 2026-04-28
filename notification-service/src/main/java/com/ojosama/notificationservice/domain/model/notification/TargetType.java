package com.ojosama.notificationservice.domain.model.notification;

import lombok.Getter;

@Getter
public enum TargetType {
    OPERATION_REQUEST("운영 요청"),
    BLACKLIST_REGISTERED("블랙리스트 처리"),
    BLIND_REGISTERED("블라인드 처리"),
    REPORT_CREATED("신고"),

    EVENT_REMINDER("행사 일정 임박"),
    EVENT_CANCELED("행사 취소"),
    EVENT_CHANGED("행사 정보 변경"),
    EVENT_REQUEST("행사 요청"),
    EVENT_REQUEST_RESULT("행사 요청 결과"),
    TICKETING_REMINDER("티켓팅 일정 임박");

    private final String description;

    TargetType(String description) {
        this.description = description;
    }

}
