package com.ojosama.moderation.domain.exception;

import com.ojosama.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum AiModerationErrorCode implements ErrorCode {
    AI_ALREADY_MODERATED(HttpStatus.BAD_REQUEST, "이미 검증된 항목입니다.");

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
