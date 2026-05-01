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
    public Optional<OperationRequest> findByIdAndDeletedAtIsNull(UUID id) {
        return operationRequestJpaRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public Page<OperationRequest> findAllByDeletedAtIsNull(Pageable pageable) {
        return operationRequestJpaRepository.findAllByDeletedAtIsNull(pageable);
    }

    @Override
    public Page<OperationRequest> findByStatusAndDeletedAtIsNull(OperationRequestStatus status, Pageable pageable){
        return operationRequestJpaRepository.findByStatusAndDeletedAtIsNull(status, pageable);
    }
}
