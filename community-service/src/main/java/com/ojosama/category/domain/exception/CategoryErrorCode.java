package com.ojosama.category.domain.exception;

import com.ojosama.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CategoryErrorCode implements ErrorCode {
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),
    CATEGORY_NAME_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 카테고리명입니다."),

    COMMUNITY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 커뮤니티입니다."),
    COMMUNITY_ALREADY_JOINED(HttpStatus.BAD_REQUEST, "이미 가입된 커뮤니티입니다."),
    COMMUNITY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "커뮤니티 접근 권한이 없습니다."),

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "올바르지 않은 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 에러가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    CategoryErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
