package com.ojosama.chatservice.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SendMessageWsRequest(
        @NotNull UUID chatRoomId,
        @NotNull UUID userId,
        @NotBlank String writerNickname,
        @NotBlank String content
) {
}
