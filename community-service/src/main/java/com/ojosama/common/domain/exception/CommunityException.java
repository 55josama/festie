package com.ojosama.common.domain.exception;

import com.ojosama.common.exception.CustomException;

public class CommunityException extends CustomException {
    public CommunityException(CommunityErrorCode code) {
        super(code);
    }
}
