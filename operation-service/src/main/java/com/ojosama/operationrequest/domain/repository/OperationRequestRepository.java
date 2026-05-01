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

    Page<OperationRequest> findAllByDeletedAtIsNull(Pageable pageable);

    Page<OperationRequest> findByStatusAndDeletedAtIsNull(OperationRequestStatus status, Pageable pageable);
}
