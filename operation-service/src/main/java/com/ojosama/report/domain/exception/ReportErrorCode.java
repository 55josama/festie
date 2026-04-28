package com.ojosama.report.domain.exception;

import com.ojosama.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ReportErrorCode implements ErrorCode {
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "신고 요청을 찾을 수 없습니다."),
    REPORT_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 신고 요청입니다."),
    REPORT_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "이미 처리된 신고 요청입니다."),
    DUPLICATE_REPORT(HttpStatus.BAD_REQUEST, "중복된 신고 요청입니다.");

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
