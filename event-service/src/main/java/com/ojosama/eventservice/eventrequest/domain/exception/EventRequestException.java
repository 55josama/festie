package com.ojosama.eventservice.eventrequest.domain.exception;

import com.ojosama.common.exception.CustomException;
import com.ojosama.common.exception.ErrorCode;

public class EventRequestException extends CustomException {
    public EventRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}
