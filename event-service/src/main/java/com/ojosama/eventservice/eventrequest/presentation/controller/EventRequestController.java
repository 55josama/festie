package com.ojosama.eventservice.eventrequest.presentation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.common.response.PageResponse;
import com.ojosama.eventservice.eventrequest.application.dto.command.CreateEventRequestCommand;
import com.ojosama.eventservice.eventrequest.application.dto.command.EventRequestListCommand;
import com.ojosama.eventservice.eventrequest.application.dto.result.EventRequestResult;
import com.ojosama.eventservice.eventrequest.application.service.EventRequestCommandService;
import com.ojosama.eventservice.eventrequest.application.service.EventRequestQueryService;
import com.ojosama.eventservice.eventrequest.presentation.dto.request.CreateEventRequestRequest;
import com.ojosama.eventservice.eventrequest.presentation.dto.request.RejectEventRequestRequest;
import com.ojosama.eventservice.eventrequest.presentation.dto.response.EventRequestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/event-requests")
@Tag(name = "행사 신청 API", description = "행사 등록 요청 및 승인/반려 처리")
public class EventRequestController {

    private final EventRequestCommandService eventRequestCommandService;
    private final EventRequestQueryService eventRequestQueryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER')")
    @Operation(summary = "행사 등록 요청", description = "매니저가 행사 등록을 관리자에게 요청하는 API입니다. 요청이 생성되면 PENDING 상태로 시작되며, 관리자의 승인 또는 반려를 기다립니다. 제목, 카테고리, 링크는 필수 입력입니다.")
    public ResponseEntity<ApiResponse<EventRequestResponse>> createEventRequest(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreateEventRequestRequest request) {

        EventRequestResult result = eventRequestCommandService.createEventRequest(
                CreateEventRequestCommand.from(UUID.fromString(userId), request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(EventRequestResponse.from(result)));
    }

    @DeleteMapping("/{requestId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "요청 취소 (작성자)", description = "본인이 등록한 요청을 취소하는 API입니다. PENDING(대기 중) 상태의 요청만 취소할 수 있으며, 타인의 요청은 취소할 수 없습니다. 취소된 요청은 CANCELLED 상태로 변경되며, 삭제되지 않고 목록에서 계속 조회됩니다.")
    public ResponseEntity<Void> cancelEventRequest(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable UUID requestId) {

        eventRequestCommandService.cancelEventRequest(UUID.fromString(userId), requestId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/admin/{requestId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "요청 삭제 (관리자)", description = "관리자 전용 API입니다. 요청의 소유자나 상태에 관계없이 강제로 삭제할 수 있습니다. 소프트 딜리트 방식으로 처리되며, 삭제 후 목록 및 상세 조회에서 노출되지 않습니다.")
    public ResponseEntity<Void> adminCancelEventRequest(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable UUID requestId) {

        eventRequestCommandService.adminCancelEventRequest(UUID.fromString(userId), requestId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{requestId}/approval")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER')")
    @Operation(summary = "요청 승인", description = "매니저/관리자 전용 API입니다. PENDING(대기 중) 상태의 행사 등록 요청을 승인합니다. 승인된 요청은 APPROVED 상태로 변경됩니다. 이미 승인 또는 반려된 요청에 대해 요청 시 409를 반환합니다.")
    public ResponseEntity<ApiResponse<EventRequestResponse>> approveEventRequest(
            @PathVariable UUID requestId) {

        EventRequestResult result = eventRequestCommandService.approveEventRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success(EventRequestResponse.from(result)));
    }

    @PostMapping("/{requestId}/rejections")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER')")
    @Operation(summary = "요청 반려", description = "매니저/관리자 전용 API입니다. PENDING(대기 중) 상태의 행사 등록 요청을 반려합니다. 반려 시 사유(rejectReason)는 필수 입력이며, 신청자에게 안내됩니다. 반려된 요청은 REJECTED 상태로 변경됩니다. 이미 처리된 요청에 대해 요청 시 409를 반환합니다.")
    public ResponseEntity<ApiResponse<EventRequestResponse>> rejectEventRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody RejectEventRequestRequest request) {

        EventRequestResult result = eventRequestCommandService.rejectEventRequest(requestId, request.rejectReason());
        return ResponseEntity.ok(ApiResponse.success(EventRequestResponse.from(result)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER')")
    @Operation(summary = "요청 목록 조회 (관리자)", description = "매니저/관리자 전용 API입니다. 전체 행사 등록 요청 목록을 조회합니다. 상태(status), 카테고리명(categoryName), 행사명(eventName), 신청자 ID(requesterId), 생성일 범위(createdStart, createdEnd)로 필터링할 수 있습니다. 기본 정렬은 생성일 내림차순이며 페이지당 10건입니다.")
    public ResponseEntity<ApiResponse<PageResponse<EventRequestResponse>>> getEventRequests(
            @ModelAttribute EventRequestListCommand query,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<EventRequestResult> result = eventRequestQueryService.getEventRequests(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result.map(EventRequestResponse::from))));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER')")
    @Operation(summary = "내 요청 목록 조회", description = "본인이 신청한 행사 등록 요청 목록을 조회합니다. 인증 헤더(X-User-Id)를 기준으로 본인 요청만 반환되며, 타인의 요청은 조회되지 않습니다.")
    public ResponseEntity<ApiResponse<PageResponse<EventRequestResponse>>> getMyEventRequests(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @ModelAttribute EventRequestListCommand query,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<EventRequestResult> result = eventRequestQueryService.getMyEventRequests(UUID.fromString(userId), query, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result.map(EventRequestResponse::from))));
    }

    @GetMapping("/{requestId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER')")
    @Operation(summary = "요청 상세 조회 (관리자)", description = "매니저/관리자 전용 API입니다. 특정 행사 등록 요청의 상세 정보를 조회합니다. 존재하지 않는 요청 ID로 조회 시 404를 반환합니다.")
    public ResponseEntity<ApiResponse<EventRequestResponse>> getEventRequest(
            @PathVariable UUID requestId) {

        EventRequestResult result = eventRequestQueryService.getEventRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success(EventRequestResponse.from(result)));
    }

    @GetMapping("/me/{requestId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "내 요청 상세 조회", description = "본인이 신청한 특정 행사 등록 요청의 상세 정보를 조회합니다. 타인의 요청 ID로 조회 시 접근이 거부됩니다.")
    public ResponseEntity<ApiResponse<EventRequestResponse>> getMyEventRequest(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable UUID requestId) {

        EventRequestResult result = eventRequestQueryService.getMyEventRequest(UUID.fromString(userId), requestId);
        return ResponseEntity.ok(ApiResponse.success(EventRequestResponse.from(result)));
    }
}
