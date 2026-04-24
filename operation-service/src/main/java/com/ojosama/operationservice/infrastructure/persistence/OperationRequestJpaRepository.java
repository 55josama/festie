package com.ojosama.operationservice.infrastructure.persistence;

import com.ojosama.operationservice.domain.model.entity.OperationRequest;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationRequestJpaRepository extends JpaRepository<OperationRequest, UUID> {
}
