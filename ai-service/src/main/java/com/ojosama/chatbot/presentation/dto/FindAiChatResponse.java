package com.ojosama.chatbot.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FindAiChatResponse (
        String answer
){ }
