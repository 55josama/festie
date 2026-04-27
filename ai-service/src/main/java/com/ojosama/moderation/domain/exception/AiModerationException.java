package com.ojosama.moderation.domain.exception;

import com.ojosama.common.exception.CustomException;
import com.ojosama.common.exception.ErrorCode;

public class AiModerationException extends CustomException {
    public AiModerationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
