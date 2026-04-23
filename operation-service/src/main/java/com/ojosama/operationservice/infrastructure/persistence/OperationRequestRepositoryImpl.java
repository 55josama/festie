package com.ojosama.operationservice.infrastructure.persistence;

import com.ojosama.operationservice.domain.model.entity.OperationRequest;
import com.ojosama.operationservice.domain.repository.OperationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OperationRequestRepositoryImpl implements OperationRequestRepository {
    private final OperationRequestJpaRepository operationRequestJpaRepository;

    @Override
    public OperationRequest save(OperationRequest operationRequest){
        return operationRequestJpaRepository.save(operationRequest);
    }
}
