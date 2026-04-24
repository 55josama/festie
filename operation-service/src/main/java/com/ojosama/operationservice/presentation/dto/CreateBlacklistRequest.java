package com.ojosama.operationservice.presentation.dto;

import com.ojosama.operationservice.application.dto.command.CreateBlacklistCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateBlacklistRequest {
    @NotNull(message = "차단 대상 유저 ID를 입력해주세요.")
    private UUID userId;

    @NotBlank(message = "블랙리스트 등록 사유를 입력해주세요.")
    private String reason;

    public CreateBlacklistCommand toCommand() {
        return new CreateBlacklistCommand(userId, reason);
    }
}
