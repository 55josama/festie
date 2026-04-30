package com.ojosama.chatbot.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAiChatRequest(
        @NotBlank(message = "질문을 입력해주세요.")
        String question
) { }
