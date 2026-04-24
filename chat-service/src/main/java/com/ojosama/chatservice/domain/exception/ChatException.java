package com.ojosama.chatservice.domain.exception;

import com.ojosama.common.exception.CustomException;

public class ChatException extends CustomException {
    public ChatException(ChatErrorCode code) {
        super(code);
    }
}