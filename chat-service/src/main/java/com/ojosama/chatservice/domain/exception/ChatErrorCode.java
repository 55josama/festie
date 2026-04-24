package com.ojosama.chatservice.domain.exception;

import com.ojosama.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ChatErrorCode implements ErrorCode {

    MESSAGE_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "ACTIVE 상태에서만 블라인드할 수 있습니다."),
    INVALID_ADMIN_ID(HttpStatus.BAD_REQUEST, "adminId는 필수입니다."),
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_INVALID_TIME(HttpStatus.BAD_REQUEST, "종료 시간은 오픈 시간보다 이후여야 합니다.");

    private final HttpStatus status;
    private final String message;

    ChatErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}
