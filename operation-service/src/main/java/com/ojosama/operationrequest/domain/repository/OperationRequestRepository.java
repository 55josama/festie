package com.ojosama.operationrequest.domain.repository;

import com.ojosama.operationrequest.domain.model.entity.OperationRequest;
import com.ojosama.operationrequest.domain.model.enums.OperationRequestStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OperationRequestRepository {
    OperationRequest save(OperationRequest operationRequest);

    Optional<OperationRequest> findByIdAndDeletedAtIsNull(UUID id);

    Optional<OperationRequest> findByIdAndRequesterIdAndDeletedAtIsNull(UUID id, UUID requesterId);

    Page<OperationRequest> findAllByDeletedAtIsNull(Pageable pageable);

    Page<OperationRequest> findByStatusAndDeletedAtIsNull(OperationRequestStatus status, Pageable pageable);

    Page<OperationRequest> findByRequesterIdAndDeletedAtIsNull(UUID requesterId, Pageable pageable);

    Page<OperationRequest> findByRequesterIdAndStatusAndDeletedAtIsNull(UUID requesterId, OperationRequestStatus status, Pageable pageable);
}
