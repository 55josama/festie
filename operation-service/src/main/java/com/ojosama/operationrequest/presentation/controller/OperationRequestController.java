package com.ojosama.operationrequest.presentation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.operationrequest.application.dto.query.ListOperationRequestQuery;
import com.ojosama.operationrequest.application.dto.result.OperationRequestResult;
import com.ojosama.operationrequest.application.service.OperationRequestService;
import com.ojosama.operationrequest.domain.model.enums.OperationRequestStatus;
import com.ojosama.operationrequest.presentation.dto.CreateOperationRequest;
import com.ojosama.operationrequest.presentation.dto.FindOperationResponse;
import com.ojosama.operationrequest.presentation.dto.ListOperationResponse;
import com.ojosama.operationrequest.presentation.dto.UpdateOperationRequest;
import com.ojosama.operationrequest.presentation.dto.UpdateOperationStatusRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/operation-requests")
@RequiredArgsConstructor
public class OperationRequestController {
    private final OperationRequestService operationRequestService;

    // 운영 요청 등록 (사용자)
    @PostMapping
    public ResponseEntity<ApiResponse<FindOperationResponse>> createOperationRequest(
            @Valid @RequestBody CreateOperationRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "11111111-1111-1111-1111-111111111111") UUID currentUserId
    ) {
        OperationRequestResult result = operationRequestService.createOperationRequest(request.toCommand(currentUserId));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(FindOperationResponse.from(result)));
    }

    // 운영 요청 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ListOperationResponse>>> getOperationRequestList(
            @RequestParam(required = false) OperationRequestStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        ListOperationRequestQuery query = new ListOperationRequestQuery(status);
        Page<ListOperationResponse> response = operationRequestService.getOperationRequestList(query, pageable)
                .map(ListOperationResponse::from);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 운영 요청 상세 조회
    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponse<FindOperationResponse>> getOperationRequest(@PathVariable UUID requestId) {
        OperationRequestResult result = operationRequestService.getOperationRequestInfo(requestId);
        return ResponseEntity.ok(ApiResponse.success(FindOperationResponse.from(result)));
    }

    // 운영 요청 수정 (요청 작성자 본인)
    @PatchMapping("/{requestId}")
    public ResponseEntity<ApiResponse<FindOperationResponse>> updateOperationRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody UpdateOperationRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "11111111-1111-1111-1111-111111111111") UUID currentUserId
    ) {
        OperationRequestResult result = operationRequestService.updateOperationRequest(requestId, request.toCommand(currentUserId));
        return ResponseEntity.ok(ApiResponse.success(FindOperationResponse.from(result)));
    }

    // 운영 요청 상태 처리 (관리자)
    // @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{requestId}/status")
    public ResponseEntity<ApiResponse<FindOperationResponse>> updateOperationRequestStatus(
            @PathVariable UUID requestId,
            @Valid @RequestBody UpdateOperationStatusRequest request
    ) {
        OperationRequestResult result = operationRequestService.updateOperationRequestStatus(requestId, request.toCommand());
        return ResponseEntity.ok(ApiResponse.success(FindOperationResponse.from(result)));
    }

    // 운영 요청 삭제 (작성자 또는 관리자)
    @DeleteMapping("/{requestId}")
    public ResponseEntity<ApiResponse<Void>> deleteOperationRequest(
            @PathVariable UUID requestId,
            @RequestHeader(value = "X-User-Id", defaultValue = "11111111-1111-1111-1111-111111111111") UUID currentUserId,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String role
    ) {
        boolean isAdmin = role.contains("ADMIN");
        operationRequestService.deleteOperationRequest(requestId, currentUserId, isAdmin);

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}