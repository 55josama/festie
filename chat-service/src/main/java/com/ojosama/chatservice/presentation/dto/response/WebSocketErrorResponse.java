package com.ojosama.chatservice.presentation.dto.response;

import java.time.LocalDateTime;

public record WebSocketErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp
) {
    public static WebSocketErrorResponse of(int status, String message) {
        return new WebSocketErrorResponse(status, message, LocalDateTime.now());
    }
}
