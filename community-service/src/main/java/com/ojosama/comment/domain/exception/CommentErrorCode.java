package com.ojosama.comment.domain.exception;

import com.ojosama.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CommentErrorCode implements ErrorCode {
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
    PARENT_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "부모 댓글을 찾을 수 없습니다."),
    COMMENT_BLOCKED(HttpStatus.FORBIDDEN, "차단된 댓글입니다."),
    POST_MISMATCH(HttpStatus.BAD_REQUEST, "부모 댓글이 해당 게시글에 속하지 않습니다."),
    COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "댓글에 대한 권한이 없습니다."),
    LIKE_COUNT_CANNOT_BE_NEGATIVE(HttpStatus.CONFLICT, "좋아요 수는 0 미만이 될 수 없습니다."),
    REPLY_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "대댓글에는 답글을 달 수 없습니다.");
    private final HttpStatus status;
    private final String message;

    CommentErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
