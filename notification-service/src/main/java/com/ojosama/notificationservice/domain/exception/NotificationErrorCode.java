package com.ojosama.notificationservice.domain.exception;

import com.ojosama.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {
    EMAIL_SEND_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송 중 오류가 발생했습니다."),

    INVALID_TARGET_ID(HttpStatus.BAD_REQUEST, "알림 대상의 식별자(ID)가 누락되었습니다."),
    INVALID_TARGET_TYPE(HttpStatus.BAD_REQUEST, "알림 종류(TYPE) 정보가 누락되었습니다."),

    NOT_FOUND_NOTIFICATION(HttpStatus.NOT_FOUND, "알림이 존재하지 않습니다.");


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
