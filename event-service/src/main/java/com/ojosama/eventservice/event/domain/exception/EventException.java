package com.ojosama.eventservice.event.domain.exception;

import com.ojosama.common.exception.CustomException;
import com.ojosama.common.exception.ErrorCode;

public class EventException extends CustomException {
    public EventException(ErrorCode errorCode) {
        super(errorCode);
    }
}
