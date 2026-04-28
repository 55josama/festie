package com.ojosama.operationrequest.domain.exception;

import com.ojosama.common.exception.CustomException;
import com.ojosama.common.exception.ErrorCode;

public class OperationRequestException extends CustomException {
    public OperationRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}
