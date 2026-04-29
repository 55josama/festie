package com.ojosama.favoriteservice.domain.exception;

import com.ojosama.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum FavoriteErrorCode implements ErrorCode {

    FAVORITE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재 하지 않는 찜입니다."),
    EXIST_FAVORITE(HttpStatus.CONFLICT, "이미 존재하는 찜입니다."),

    INVALID_EVENT_ID(HttpStatus.CONFLICT, "행사 ID는 필수입니다."),
    INVALID_EVENT_NAME(HttpStatus.CONFLICT, "행사 이름은 필수입니다."),

    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "이벤트를 찾지 못했습니다.");

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
