package com.ojosama.operationservice.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReportCommand {
    @NotNull(message = "제재 확정 여부를 선택해주세요.")
    private Boolean isResolved;

    @NotBlank(message = "관리자 처리 사유를 입력해주세요.")
    private String operatorMemo;
}
