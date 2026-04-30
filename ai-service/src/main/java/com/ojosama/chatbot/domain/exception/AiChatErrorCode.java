package com.ojosama.chatbot.domain.exception;

import com.ojosama.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum AiChatErrorCode implements ErrorCode {
    DOCUMENT_INDEXING_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "문서 인덱싱 실패"),
    OPEN_AI_API_COMMUNICATION_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "Open AI API 통신 에러"),
    KAFKA_MESSAGE_PARSING_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "Kafka Event 메시지 파싱 및 역직렬화 실패");

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
