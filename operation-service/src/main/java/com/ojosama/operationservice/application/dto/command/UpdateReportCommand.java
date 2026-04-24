package com.ojosama.operationservice.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReportCommand {
    private Boolean isResolved;
    private String operatorMemo;
}
