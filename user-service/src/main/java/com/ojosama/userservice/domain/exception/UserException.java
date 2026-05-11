package com.ojosama.userservice.domain.exception;

import com.ojosama.common.exception.CustomException;
import com.ojosama.common.exception.ErrorCode;

public class UserException extends CustomException {

    public UserException(ErrorCode code) {
        super(code);
    }
}