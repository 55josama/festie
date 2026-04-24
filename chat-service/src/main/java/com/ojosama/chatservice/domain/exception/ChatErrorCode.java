package com.ojosama.chatservice.domain.exception;

import com.ojosama.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ChatErrorCode implements ErrorCode {

    // ChatRoom
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 행사에 대한 채팅방이 존재합니다."),
    CHAT_ROOM_NOT_OPEN(HttpStatus.BAD_REQUEST, "오픈된 채팅방에서만 메시지를 보낼 수 있습니다."),
    CHAT_ROOM_ALREADY_OPENED(HttpStatus.BAD_REQUEST, "이미 오픈된 채팅방입니다."),
    CHAT_ROOM_ALREADY_ENDED(HttpStatus.NOT_FOUND, "이미 채팅방이 종료되었습니다."),
    CHAT_ROOM_INVALID_TIME(HttpStatus.BAD_REQUEST, "종료 시간은 오픈 시간보다 이후여야 합니다."),
    CHAT_ROOM_EVENT_ID_REQUIRED(HttpStatus.BAD_REQUEST, "eventId는 필수입니다."),

    // Message
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다."),
    MESSAGE_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "메시지가 이미 삭제되었습니다."),
    MESSAGE_ALREADY_BLINDED(HttpStatus.BAD_REQUEST, "이미 블라인드 처리된 메시지입니다."),
    MESSAGE_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "ACTIVE 상태에서만 블라인드할 수 있습니다."),
    MESSAGE_CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, "메시지 내용은 필수입니다."),
    MESSAGE_CONTENT_TOO_LONG(HttpStatus.BAD_REQUEST, "메시지 내용이 최대 길이를 초과했습니다."),
    MESSAGE_SEND_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "현재 채팅방에는 메시지를 보낼 수 없습니다."),

    // Permission
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "userId는 필수입니다."),
    INVALID_ADMIN_ID(HttpStatus.BAD_REQUEST, "adminId는 필수입니다."),
    MESSAGE_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "본인의 메시지만 삭제할 수 있습니다."),
    MESSAGE_BLIND_FORBIDDEN(HttpStatus.FORBIDDEN, "메시지 블라인드 권한이 없습니다."),
    CHAT_ROOM_CLOSE_FORBIDDEN(HttpStatus.FORBIDDEN, "채팅방 종료 권한이 없습니다.");

    private final HttpStatus status;
    private final String message;

    ChatErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}
