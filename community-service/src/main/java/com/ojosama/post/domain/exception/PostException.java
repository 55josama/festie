package com.ojosama.post.domain.exception;

import com.ojosama.common.exception.CustomException;
import com.ojosama.common.exception.ErrorCode;

public class PostException extends CustomException {
    public PostException(ErrorCode errorCode){
        super(errorCode);
    }
}
