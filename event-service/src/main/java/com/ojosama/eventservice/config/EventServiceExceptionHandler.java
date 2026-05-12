package com.ojosama.eventservice.config;

import com.ojosama.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class EventServiceExceptionHandler {

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handlePessimisticLockingFailure(PessimisticLockingFailureException e) {
        log.warn("락 획득 실패 (동시 요청): {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(HttpStatus.CONFLICT.value(), "현재 다른 요청이 처리 중입니다. 잠시 후 다시 시도해주세요."));
    }
}
