package com.ojosama.operationrequest.application.dto.query;

import com.ojosama.operationrequest.domain.model.enums.OperationRequestStatus;

public record ListOperationRequestQuery(
    OperationRequestStatus status
) { }
