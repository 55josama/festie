package com.ojosama.operationservice.domain.exception;

import com.ojosama.common.exception.CustomException;
import com.ojosama.common.exception.ErrorCode;

public class NoticeException extends CustomException {
    public NoticeException(ErrorCode errorCode) {
        super(errorCode);
    }
}
