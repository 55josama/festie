package com.ojosama.notificationservice.domain.model.notification;

public enum Target {
    EVENT_REQUEST, // 행사 요청
    EVENT_REQUEST_RESULT, // 행사 요청 결과
    REPORT_CREATED, // 행사 요청
    EVENT_REMINDER, // 행사 일정 임박
    EVENT_CANCELLED, // 행사 취소
    EVENT_CHANGED // 행사 정보 변경
}
