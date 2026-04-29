package com.ojosama.chatservice.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateMessageRequest(
        @NotBlank(message = "메시지 내용은 필수입니다.")
        String content
) {
}
