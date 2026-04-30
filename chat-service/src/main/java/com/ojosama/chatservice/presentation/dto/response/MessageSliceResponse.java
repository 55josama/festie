package com.ojosama.chatservice.presentation.dto.response;

import com.ojosama.chatservice.application.dto.result.MessageSliceResult;
import java.util.List;

public record MessageSliceResponse(
        List<MessageResponse> messages,
        boolean hasNext
) {
    public static MessageSliceResponse from(MessageSliceResult result) {
        return new MessageSliceResponse(
                result.messages().stream()
                        .map(MessageResponse::from)
                        .toList(),
                result.hasNext()
        );
    }
}
