package com.ojosama.operationrequest.presentation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.common.response.PageResponse;
import com.ojosama.operationrequest.application.dto.query.ListOperationRequestQuery;
import com.ojosama.operationrequest.application.dto.result.OperationRequestResult;
import com.ojosama.operationrequest.application.service.OperationRequestService;
import com.ojosama.operationrequest.domain.model.enums.OperationRequestStatus;
import com.ojosama.operationrequest.presentation.dto.CreateOperationRequest;
import com.ojosama.operationrequest.presentation.dto.FindOperationResponse;
import com.ojosama.operationrequest.presentation.dto.ListOperationResponse;
import com.ojosama.operationrequest.presentation.dto.UpdateOperationRequest;
import com.ojosama.operationrequest.presentation.dto.UpdateOperationStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/operation-requests")
@RequiredArgsConstructor
@Tag(name = "운영 요청", description = "운영 요청 관리 API")
public class OperationRequestController {
    private final OperationRequestService operationRequestService;

    // 운영 요청 등록 (사용자)
    @Operation(
            summary = "운영 요청 등록",
            description = "사용자가 게시판에 새로운 운영 관련 요청을 등록합니다. <br>" +
                    "일반 사용자만 접근 가능합니다."
    )
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<ApiResponse<FindOperationResponse>> createOperationRequest(
            @Valid @RequestBody CreateOperationRequest request,
            @AuthenticationPrincipal UUID currentUserId
    ) {
        OperationRequestResult result = operationRequestService.createOperationRequest(
                request.toCommand(currentUserId));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(FindOperationResponse.from(result)));
    }

    // 운영 요청 목록 조회
    @Operation(
            summary = "운영 요청 목록 조회",
            description = "운영 요청 목록을 조회합니다. <br>" +
                    "상태별 필터링이 가능하며, 페이징 처리됩니다. <br>" +
                    "관리자만 접근 가능합니다."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ListOperationResponse>>> getOperationRequestList(
            @RequestParam(required = false) OperationRequestStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        ListOperationRequestQuery query = new ListOperationRequestQuery(status);
        PageResponse<OperationRequestResult> serviceResult = operationRequestService.getOperationRequestList(query, pageable);

        PageResponse<ListOperationResponse> response = new PageResponse<>(
                serviceResult.content().stream()
                        .map(ListOperationResponse::from)
                        .toList(),
                serviceResult.page(),
                serviceResult.size(),
                serviceResult.totalElements(),
                serviceResult.totalPages()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 운영 요청 목록 조회 (작성자 본인)
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PageResponse<ListOperationResponse>>> getMyOperationRequestList(
            @AuthenticationPrincipal UUID currentUserId,
            @RequestParam(required = false) OperationRequestStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        ListOperationRequestQuery query = new ListOperationRequestQuery(status);

        PageResponse<ListOperationResponse> response = PageResponse.from(
                operationRequestService.getMyOperationRequestList(currentUserId, query, pageable)
                        .map(ListOperationResponse::from)
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 운영 요청 상세 조회
    @Operation(
            summary = "운영 요청 상세 조회",
            description = "특정 운영 요청의 상세 정보를 조회합니다. <br>" +
                    "관리자만 접근 가능합니다."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponse<FindOperationResponse>> getOperationRequest(@PathVariable UUID requestId) {
        OperationRequestResult result = operationRequestService.getOperationRequestInfo(requestId);
        return ResponseEntity.ok(ApiResponse.success(FindOperationResponse.from(result)));
    }

    // 운영 요청 상세 조회 (작성자 본인)
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me/{requestId}")
    public ResponseEntity<ApiResponse<FindOperationResponse>> getMyOperationRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal UUID currentUserId
    ) {
        OperationRequestResult result = operationRequestService.getMyOperationRequestInfo(requestId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(FindOperationResponse.from(result)));
    }

    // 운영 요청 수정 (요청 작성자 본인)
    @Operation(
            summary = "운영 요청 수정",
            description = "운영 요청 내용을 수정합니다. <br>" +
                    "요청 작성자 본인 또는 관리자만 접근 가능합니다. <br>" +
                    "작성자는 운영 요청 상태가 대기중(PENDING)일 때만 수정 가능합니다."
    )
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PatchMapping("/{requestId}")
    public ResponseEntity<ApiResponse<FindOperationResponse>> updateOperationRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody UpdateOperationRequest request,
            @AuthenticationPrincipal UUID currentUserId
    ) {
        boolean isAdmin = isCurrentUserAdmin();
        OperationRequestResult result = operationRequestService.updateOperationRequest(
                requestId,
                request.toCommand(currentUserId),
                isAdmin
        );
        return ResponseEntity.ok(ApiResponse.success(FindOperationResponse.from(result)));
    }

    // 운영 요청 상태 처리 (관리자)
    @Operation(
            summary = "운영 요청 상태 처리",
            description = "운영 요청의 상태를 변경합니다. (예: 대기중 → 처리중 → 완료) <br>" +
                    "관리자만 접근 가능합니다."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{requestId}/status")
    public ResponseEntity<ApiResponse<FindOperationResponse>> updateOperationRequestStatus(
            @PathVariable UUID requestId,
            @Valid @RequestBody UpdateOperationStatusRequest request
    ) {
        OperationRequestResult result = operationRequestService.updateOperationRequestStatus(requestId,
                request.toCommand());
        return ResponseEntity.ok(ApiResponse.success(FindOperationResponse.from(result)));
    }

    // 운영 요청 삭제 (작성자 또는 관리자)
    @Operation(
            summary = "운영 요청 삭제",
            description = "운영 요청을 삭제합니다. <br>" +
                    "작성자 본인 또는 관리자만 접근 가능합니다. <br>" +
                    "작성자는 운영 요청 상태가 대기중(PENDING)일 때만 삭제 가능합니다."
    )
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @DeleteMapping("/{requestId}")
    public ResponseEntity<ApiResponse<Void>> deleteOperationRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal UUID currentUserId
    ) {
        boolean isAdmin = isCurrentUserAdmin();
        operationRequestService.deleteOperationRequest(requestId, currentUserId, isAdmin);

        return ResponseEntity.ok(ApiResponse.deleted());
    }

    private boolean isCurrentUserAdmin() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}
