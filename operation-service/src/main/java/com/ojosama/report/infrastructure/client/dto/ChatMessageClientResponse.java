package com.ojosama.report.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.sql.Timestamp;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatMessageClientResponse(
        Integer status,
        String message,
        ChatData data
) {
    public UUID userId() {
        return data != null ? data.userId() : null;
    }

    public String category() {
        return data != null ? data.category() : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatData(
            UUID messageId,
            UUID chatRoomId,
            String category,
            UUID userId,
            String content,
            String status
    ) { }
}
