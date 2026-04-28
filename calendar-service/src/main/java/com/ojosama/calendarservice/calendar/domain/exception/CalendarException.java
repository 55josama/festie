package com.ojosama.calendarservice.calendar.domain.exception;

import com.ojosama.common.exception.CustomException;
import com.ojosama.common.exception.ErrorCode;

public class CalendarException extends CustomException {

    public CalendarException(ErrorCode errorCode) {
        super(errorCode);
    }
}
