package com.ojosama.operationservice.domain.exception;

import com.ojosama.common.exception.CustomException;
import com.ojosama.common.exception.ErrorCode;

public class BlacklistException extends CustomException {
    public BlacklistException(ErrorCode errorCode) {
        super(errorCode);
    }
}
