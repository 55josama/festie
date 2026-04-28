package com.ojosama.common.domain.exception;

import com.ojosama.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CommunityErrorCode implements ErrorCode {
    INVALID_CONTENT(HttpStatus.BAD_REQUEST, "본문은 비어있을 수 없습니다."),
    CONTENT_TOO_SHORT(HttpStatus.BAD_REQUEST, "본문은 최소 2자 이상이어야 합니다."),
    CONTENT_TOO_LONG(HttpStatus.BAD_REQUEST, "본문은 4000자 이하여야 합니다."),
    BANNED_WORD_DETECTED(HttpStatus.BAD_REQUEST, "금지어가 포함되어 있습니다.");
    private final HttpStatus status;
    private final String message;

    CommunityErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
