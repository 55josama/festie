package com.ojosama.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {

    private final HttpStatus status;
    private final String message;

    public CustomException(ErrorCode code) {
        super(code.getMessage());
        this.status = code.getStatus();
        this.message = code.getMessage();
    }
}
