package com.ojosama.chatservice.presentation.dto.response;

import com.ojosama.chatservice.application.dto.result.MessageSliceResult;
import java.util.List;

public record MessageSliceResponse(
        List<MessageResponse> messages,
        boolean hasNext
) {
    public static MessageSliceResponse from(MessageSliceResult result) {
        return from(result, true);
    }

    public static MessageSliceResponse from(MessageSliceResult result, boolean maskBlindedContent) {
        return new MessageSliceResponse(
                result.messages().stream()
                        .map(message -> MessageResponse.from(message, maskBlindedContent))
                        .toList(),
                result.hasNext()
        );
    }
}
