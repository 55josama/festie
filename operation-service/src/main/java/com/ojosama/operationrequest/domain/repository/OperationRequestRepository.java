package com.ojosama.operationrequest.domain.repository;

import com.ojosama.operationrequest.domain.model.entity.OperationRequest;

public interface OperationRequestRepository {
    OperationRequest save(OperationRequest operationRequest);
}
