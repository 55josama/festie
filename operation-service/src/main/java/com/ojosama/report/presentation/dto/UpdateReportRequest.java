package com.ojosama.report.presentation.dto;

import com.ojosama.report.application.dto.command.UpdateReportCommand;
import com.ojosama.report.domain.model.enums.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateReportRequest(
        @NotNull(message = "제재 확정 여부를 선택해주세요.")
        ReportStatus status,

        @NotBlank(message = "관리자 처리 사유를 입력해주세요.")
        String operatorMemo
) {
    public UpdateReportCommand toCommand() {
        return new UpdateReportCommand(status, operatorMemo);
    }
}