package com.ojosama.userservice.presentation.dto.request;

import com.ojosama.userservice.application.dto.command.ReissueTokenCommand;
import jakarta.validation.constraints.NotBlank;

public record ReissueTokenRequestDto(
        @NotBlank(message = "Refresh Token은 필수입니다.")
        String refreshToken
) {
    public ReissueTokenCommand toCommand() {
        return new ReissueTokenCommand(refreshToken);
    }
}