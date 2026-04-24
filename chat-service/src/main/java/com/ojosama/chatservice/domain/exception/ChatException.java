package com.ojosama.chatservice.domain.exception;

import com.ojosama.common.exception.CustomException;
import com.ojosama.common.exception.ErrorCode;

public class ChatException extends CustomException {
    public ChatException(ErrorCode code) {
        super(code);
    }
}