package com.ojosama.operationservice.presentation.dto;

import com.ojosama.operationservice.application.dto.command.UpdateBlacklistCommand;
import com.ojosama.operationservice.domain.model.enums.BlacklistStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBlacklistRequest {
    @NotNull(message = "블랙리스트 상태를 입력해주세요.")
    private BlacklistStatus status;

    @NotBlank(message = "블랙리스트 등록/해제 사유를 입력해주세요.")
    private String reason;

    public UpdateBlacklistCommand toCommand() {
        return new UpdateBlacklistCommand(status, reason);
    }
}
