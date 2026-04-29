package com.ojosama.chatservice.application.dto.result;

import java.util.List;

public record MessageSliceResult(
        List<MessageResult> messages,
        boolean hasNext
) {
}
