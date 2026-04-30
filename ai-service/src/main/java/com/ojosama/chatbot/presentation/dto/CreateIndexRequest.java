package com.ojosama.chatbot.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateIndexRequest(
        @NotBlank(message = "문서에 등록할 내용을 입력해주세요.")
        String content
) { }
