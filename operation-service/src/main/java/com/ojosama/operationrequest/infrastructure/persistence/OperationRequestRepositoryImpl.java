package com.ojosama.operationrequest.infrastructure.persistence;

import com.ojosama.operationrequest.domain.model.entity.OperationRequest;
import com.ojosama.operationrequest.domain.model.enums.OperationRequestStatus;
import com.ojosama.operationrequest.domain.repository.OperationRequestRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OperationRequestRepositoryImpl implements OperationRequestRepository {
    private final OperationRequestJpaRepository operationRequestJpaRepository;

    @Override
    public OperationRequest save(OperationRequest operationRequest){
        return operationRequestJpaRepository.save(operationRequest);
    }

    @Override
    public Optional<OperationRequest> findById(UUID id) {
        return operationRequestJpaRepository.findById(id);
    }

    @Override
    public Page<OperationRequest> findAll(Pageable pageable) {
        return operationRequestJpaRepository.findAll(pageable);
    }

    @Override
    public Page<OperationRequest> findByStatus(OperationRequestStatus status, Pageable pageable){
        return operationRequestJpaRepository.findByStatus(status, pageable);
    }

    @Override
    public void delete(OperationRequest operationRequest){
        operationRequestJpaRepository.delete(operationRequest);
    }
}
