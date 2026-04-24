package com.ojosama.operationservice.domain.repository;

import com.ojosama.operationservice.domain.model.entity.OperationRequest;

public interface OperationRequestRepository {
    OperationRequest save(OperationRequest operationRequest);
}
