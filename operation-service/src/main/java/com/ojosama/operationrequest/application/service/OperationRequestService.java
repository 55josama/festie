package com.ojosama.operationrequest.application.service;

import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.OutboxEventPublisher;
import com.ojosama.operationrequest.application.dto.command.CreateOperationRequestCommand;
import com.ojosama.operationrequest.application.dto.command.UpdateOperationRequestCommand;
import com.ojosama.operationrequest.application.dto.command.UpdateRequestStatusCommand;
import com.ojosama.operationrequest.application.dto.query.ListOperationRequestQuery;
import com.ojosama.operationrequest.application.dto.result.OperationRequestResult;
import com.ojosama.operationrequest.domain.event.payload.OperationRequestCreatedEvent;
import com.ojosama.operationrequest.domain.exception.OperationRequestErrorCode;
import com.ojosama.operationrequest.domain.exception.OperationRequestException;
import com.ojosama.operationrequest.domain.model.entity.OperationRequest;
import com.ojosama.operationrequest.domain.repository.OperationRequestRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OperationRequestService {
    private final OperationRequestRepository operationRequestRepository;
    private final OutboxEventPublisher outbox;

    // 운영 요청 생성
    @Transactional
    public OperationRequestResult createOperationRequest(CreateOperationRequestCommand command) {
        OperationRequest savedRequest = operationRequestRepository.save(command.toEntity());

        publishRequestCreatedEvent(savedRequest);

        return OperationRequestResult.from(savedRequest);
    }

    // 운영 요청 목록 조회
    public Page<OperationRequestResult> getOperationRequestList(ListOperationRequestQuery query, Pageable pageable) {
        Page<OperationRequest> requests = fetchRequestsByQuery(query, pageable);
        return requests.map(OperationRequestResult::from);
    }

    // 운영 요청 상세 조회
    public OperationRequestResult getOperationRequestInfo(UUID requestId) {
        OperationRequest request = findOperationRequestById(requestId);
        return OperationRequestResult.from(request);
    }

    // 운영 요청 수정 (사용자)
    @Transactional
    public OperationRequestResult updateOperationRequest(UUID requestId, UpdateOperationRequestCommand command) {
        OperationRequest request = findOperationRequestById(requestId);

        validateRequester(request, command.requesterId());

        request.update(command.title(), command.content());

        return OperationRequestResult.from(request);
    }

    // 운영 요청 상태 변경 (관리자)
    @Transactional
    public OperationRequestResult updateOperationRequestStatus(UUID requestId, UpdateRequestStatusCommand command) {
        OperationRequest request = findOperationRequestById(requestId);

        // 엔티티 내부에서 상태 변경 및 관리자 메모 필수 여부 검증
        request.updateStatus(command.status(), command.adminMemo());

        return OperationRequestResult.from(request);
    }

    // 운영 요청 삭제
    @Transactional
    public void deleteOperationRequest(UUID requestId, UUID userId, boolean isAdmin) {
        OperationRequest request = findOperationRequestById(requestId);

        if (!isAdmin) {
            request.validateDeletableBy(userId);
        }

        request.deleted();
    }

    private OperationRequest findOperationRequestById(UUID requestId) {
        return operationRequestRepository.findByIdAndDeletedAtIsNull(requestId)
                .orElseThrow(() -> new OperationRequestException(OperationRequestErrorCode.OPERATION_REQUEST_NOT_FOUND));
    }

    private Page<OperationRequest> fetchRequestsByQuery(ListOperationRequestQuery query, Pageable pageable) {
        if (query.status() != null) {
            return operationRequestRepository.findByStatusAndDeletedAtIsNull(query.status(), pageable);
        }
        return operationRequestRepository.findAllByDeletedAtIsNull(pageable);
    }

    // 작성자 본인인지 검증
    private void validateRequester(OperationRequest request, UUID requesterId) {
        if (!request.getRequesterId().equals(requesterId)) {
            throw new OperationRequestException(OperationRequestErrorCode.UNAUTHORIZED_UPDATE);
        }
    }

    private void publishRequestCreatedEvent(OperationRequest request) {
        OperationRequestCreatedEvent event = new OperationRequestCreatedEvent(
                request.getId(),
                request.getRequesterId(),
                request.getTitle()
        );

        outbox.publish(
                "OPERATION_REQUEST",
                request.getId(),
                EventType.OPERATION_REQUEST_CREATED,
                "operation.request.created.v1",
                event
        );
    }
}
