package com.ojosama.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ojosama.common.exception.ErrorCode;
import java.time.LocalDateTime;
import lombok.Getter;

// null 필드는 JSON 직렬화에서 제외
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final int status;
    private final String message;
    private final T data;
    private final LocalDateTime timestamp;

    private ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // 200 성공 (데이터 있음)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "성공하였습니다.", data);
    }

    // 200 성공 (데이터 없음)
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "삭제되었습니다.", null);
    }

    // 201 생성
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(201, "생성되었습니다.", data);
    }
    
    public static <T> ApiResponse<T> error(ErrorCode code) {
        return new ApiResponse<>(code.getStatus().value(), code.getMessage(), null);
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }
}