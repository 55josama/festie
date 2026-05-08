package com.ojosama.post.domain.exception;

import com.ojosama.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PostErrorCode implements ErrorCode {
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "게시글에 대한 권한이 없습니다."),
    POST_BLINDED(HttpStatus.FORBIDDEN, "차단된 게시글입니다."),

    INVALID_POST_TITLE(HttpStatus.BAD_REQUEST, "게시글 제목이 올바르지 않습니다."),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "카테고리 이름이 올바르지 않습니다."),

    LIKE_COUNT_CANNOT_BE_NEGATIVE(HttpStatus.BAD_REQUEST, "좋아요 수는 0 미만이 될 수 없습니다."),
    COMMENT_COUNT_CANNOT_BE_NEGATIVE(HttpStatus.BAD_REQUEST, "댓글 수는 0 미만이 될 수 없습니다."),

    ALREADY_LIKED(HttpStatus.CONFLICT, "이미 좋아요한 게시글입니다."),
    NOT_LIKED(HttpStatus.CONFLICT, "좋아요하지 않은 게시글입니다.");

    private final HttpStatus status;
    private final String message;

    PostErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
