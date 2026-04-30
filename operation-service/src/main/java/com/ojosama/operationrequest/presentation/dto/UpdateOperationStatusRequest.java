package com.ojosama.operationrequest.presentation.dto;

import com.ojosama.operationrequest.application.dto.command.UpdateRequestStatusCommand;
import com.ojosama.operationrequest.domain.model.enums.OperationRequestStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOperationStatusRequest(
        @NotNull(message = "상태 값은 필수입니다.")
        OperationRequestStatus status,

        String adminMemo
) {
    public UpdateRequestStatusCommand toCommand() {
        return new UpdateRequestStatusCommand(status, adminMemo);
    }
}
