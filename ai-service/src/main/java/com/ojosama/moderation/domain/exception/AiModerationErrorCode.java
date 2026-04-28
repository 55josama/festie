package com.ojosama.moderation.domain.exception;

import com.ojosama.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum AiModerationErrorCode implements ErrorCode {
    BATCH_INTEGRITY_VIOLATION(HttpStatus.INTERNAL_SERVER_ERROR, "AI 모델이 일부 항목을 누락하거나 중복 반환하여 배치를 중단합니다."),
    EVENT_PUBLISH_INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 검증 이벤트 발행 중 스레드 인터럽트가 발생했습니다."),
    EVENT_PUBLISH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 검증 평가 완료 이벤트 발행에 실패했습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
