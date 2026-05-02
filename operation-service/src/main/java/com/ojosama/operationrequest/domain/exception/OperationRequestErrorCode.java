package com.ojosama.operationrequest.domain.exception;

import com.ojosama.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum OperationRequestErrorCode implements ErrorCode {
    OPERATION_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 운영 요청을 찾을 수 없습니다."),
    OPERATION_REQUEST_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 운영 요청입니다."),
    INVALID_UPDATE_STATUS(HttpStatus.BAD_REQUEST, "대기 중(PENDING)인 상태에서만 요청을 수정할 수 있습니다."),
    INVALID_DELETE_STATUS(HttpStatus.BAD_REQUEST, "진행 중이거나 처리 완료된 요청은 삭제할 수 없습니다."),
    ADMIN_MEMO_REQUIRED(HttpStatus.BAD_REQUEST, "최초 상태 변경 시 관리자 메모는 필수입니다."),
    UNAUTHORIZED_UPDATE(HttpStatus.FORBIDDEN, "자신의 요청만 수정할 수 있습니다."),
    UNAUTHORIZED_DELETE(HttpStatus.FORBIDDEN, "자신의 요청만 삭제할 수 있습니다.");

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
