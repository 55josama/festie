package com.ojosama.operationservice.presentation.dto;

import com.ojosama.operationservice.application.dto.command.UpdateReportCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateReportRequest(
        @NotNull(message = "제재 확정 여부를 선택해주세요.")
        Boolean isResolved,

        @NotBlank(message = "관리자 처리 사유를 입력해주세요.")
        String operatorMemo
) {
    public UpdateReportCommand toCommand() {
        return new UpdateReportCommand(isResolved, operatorMemo);
    }
}
