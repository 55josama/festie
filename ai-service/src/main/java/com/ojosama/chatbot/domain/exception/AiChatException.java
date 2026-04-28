package com.ojosama.chatbot.domain.exception;

import com.ojosama.common.exception.CustomException;
import com.ojosama.common.exception.ErrorCode;

public class AiChatException extends CustomException {
    public AiChatException(ErrorCode errorCode) {
        super(errorCode);
    }
}
