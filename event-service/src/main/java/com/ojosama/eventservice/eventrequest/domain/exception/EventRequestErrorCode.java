package com.ojosama.eventservice.eventrequest.domain.exception;

import com.ojosama.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum EventRequestErrorCode implements ErrorCode {

    EVENT_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 행사 요청을 찾을 수 없습니다."),
    EVENT_REQUEST_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 행사 요청입니다."),
    EVENT_REQUEST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인의 요청만 취소할 수 있습니다."),
    EVENT_REQUEST_CANNOT_CANCEL(HttpStatus.FORBIDDEN, "승인 또는 반려된 요청은 취소할 수 없습니다."),
    EVENT_REQUEST_CATEGORY_FORBIDDEN(HttpStatus.FORBIDDEN, "이 작업을 수행할 권한이 없습니다."),
    EVENT_REQUEST_INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "시작일은 종료일보다 이전이어야 합니다.");

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
