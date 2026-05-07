package com.ojosama.userservice.domain.exception;

import com.ojosama.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "시스템 관리자를 찾을 수 없습니다."),
    CATEGORY_MANAGER_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리 담당자를 찾을 수 없습니다."),

    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),

    INVALID_LOGIN_INFO(HttpStatus.BAD_REQUEST, "이메일 혹은 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "Refresh Token이 일치하지 않습니다."),
    REFRESH_TOKEN_HASH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Refresh Token 해시 생성에 실패했습니다."),


    BLOCKED_USER(HttpStatus.FORBIDDEN, "차단된 사용자는 로그인할 수 없습니다."),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "존재하지 않는 카테고리입니다.");

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