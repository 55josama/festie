package com.ojosama.notificationservice.domain.exception;

import com.ojosama.common.exception.CustomException;

public class NotificationException extends CustomException {

    public NotificationException(NotificationErrorCode code) {
        super(code);
    }
}
