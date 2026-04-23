package com.ojosama.operationservice.domain.exception;

import com.ojosama.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum OperationRequestErrorCode implements ErrorCode {
    OPERATION_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "운영 요청을 찾을 수 없습니다."),
    OPERATION_REQUEST_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 운영 요청입니다.");

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
