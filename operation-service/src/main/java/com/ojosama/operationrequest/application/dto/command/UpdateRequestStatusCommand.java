package com.ojosama.operationrequest.application.dto.command;

import com.ojosama.operationrequest.domain.model.enums.OperationRequestStatus;

public record UpdateRequestStatusCommand(
        OperationRequestStatus status,
        String adminMemo
) { }
