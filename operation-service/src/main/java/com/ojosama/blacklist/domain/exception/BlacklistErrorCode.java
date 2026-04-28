package com.ojosama.blacklist.domain.exception;

import com.ojosama.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum BlacklistErrorCode implements ErrorCode {
    USER_ALREADY_ACTIVATED(HttpStatus.BAD_REQUEST, "이미 차단 활성화된 유저입니다."),
    BLACKLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "블랙리스트를 찾을 수 없습니다."),
    BLACKLIST_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "활성 상태인 블랙리스트만 해제할 수 있습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
