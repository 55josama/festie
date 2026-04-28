package com.ojosama.notificationservice.domain.exception;

import com.ojosama.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EmailErrorCode implements ErrorCode {
    INVALID_EMAIL_ADDRESS(HttpStatus.INTERNAL_SERVER_ERROR, "이메일ID는 필수입니다."),
    INVALID_TITLE(HttpStatus.INTERNAL_SERVER_ERROR, "제목은 필수입니다.."),
    INVALID_CONTENT(HttpStatus.INTERNAL_SERVER_ERROR, "내용은 필수입니다.");


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
