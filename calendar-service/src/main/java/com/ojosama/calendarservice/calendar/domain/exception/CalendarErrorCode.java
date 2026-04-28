package com.ojosama.calendarservice.calendar.domain.exception;

import com.ojosama.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum CalendarErrorCode implements ErrorCode {

    CALENDAR_NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 일정을 찾을 수 없습니다."),
    CALENDAR_ACCESS_DENIED(HttpStatus.FORBIDDEN, "다른 사용자의 일정은 수정할 수 없습니다."),
    CALENDAR_DELETE_DENIED(HttpStatus.FORBIDDEN, "다른 사용자의 일정은 삭제할 수 없습니다."),
    EVENT_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 행사 일정을 찾을 수 없습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),

    EXISTS_CALENDAR(HttpStatus.NOT_FOUND, "존재하는 일정입니다.");

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
