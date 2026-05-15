package com.ojosama.report.presentation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.report.application.dto.command.CreateReportCommand;
import com.ojosama.report.application.dto.query.ListReportQuery;
import com.ojosama.report.application.dto.result.ReportInfoResult;
import com.ojosama.report.application.service.ReportService;
import com.ojosama.report.domain.exception.ReportErrorCode;
import com.ojosama.report.domain.exception.ReportException;
import com.ojosama.report.domain.model.enums.ReportStatus;
import com.ojosama.report.domain.model.enums.TargetType;
import com.ojosama.report.domain.model.enums.ReporterType;
import com.ojosama.report.infrastructure.client.ChatClient;
import com.ojosama.report.infrastructure.client.CommunityClient;
import com.ojosama.report.presentation.dto.CreateReportRequest;
import com.ojosama.report.presentation.dto.FindReportResponse;
import com.ojosama.report.presentation.dto.ListReportResponse;
import com.ojosama.report.presentation.dto.UpdateReportRequest;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
@Tag(name = "신고", description = "신고 관리 API")
public class ReportController {
    private final ReportService reportService;
    private final CommunityClient communityClient;
    private final ChatClient chatClient;

    // 신고 생성
    @Operation(
            summary = "신고 생성",
            description = "게시글, 댓글, 채팅 메시지를 신고합니다. <br>" +
                    "일반 사용자만 접근 가능합니다. <br>" +
                    "신고 대상의 작성자 정보는 자동으로 조회됩니다."
    )
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<ApiResponse<FindReportResponse>> createReport(
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal UUID userId) {

        UUID targetUserId = fetchTargetUserId(request.targetType(), request.targetId());

        CreateReportCommand command = request.toCommand(userId, targetUserId);
        ReportInfoResult result = reportService.createReport(command, ReporterType.USER);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(FindReportResponse.from(result)));
    }

    // 신고 목록 조회
    @Operation(
            summary = "신고 목록 조회",
            description = "신고 목록을 조회합니다. <br>" +
                    "상태별 필터링이 가능하며, 페이징 처리됩니다. <br>" +
                    "관리자 및 각 행사 담당 매니저, 커뮤니티 매니저만 접근 가능합니다."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER', 'COMMUNITY_MANAGER')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ListReportResponse>>> getReportList(
            @RequestParam(required = false) ReportStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        ListReportQuery query = new ListReportQuery(status);

        Page<ListReportResponse> response = reportService.getReportList(query, pageable)
                .map(ListReportResponse::from);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 신고 상세 조회
    @Operation(
            summary = "신고 상세 조회",
            description = "특정 신고의 상세 정보를 조회합니다. <br>" +
                    "관리자 및 각 행사 담당 매니저, 커뮤니티 매니저만 접근 가능합니다."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER', 'COMMUNITY_MANAGER')")
    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<FindReportResponse>> getReportInfo(
            @PathVariable UUID reportId) {

        var result = reportService.getReportInfo(reportId);
        return ResponseEntity.ok(ApiResponse.success(FindReportResponse.from(result)));
    }

    // 신고 처리
    @Operation(
            summary = "신고 처리",
            description = "신고를 검토하고 상태를 변경합니다. (예: 대기중 → 승인/반려) <br>" +
                    "관리자만 접근 가능합니다."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{reportId}/status")
    public ResponseEntity<ApiResponse<FindReportResponse>> updateReportStatus(
            @PathVariable UUID reportId,
            @Valid @RequestBody UpdateReportRequest request) {

        ReportInfoResult result = reportService.updateReport(reportId, request.toCommand());
        return ResponseEntity.ok(ApiResponse.success(FindReportResponse.from(result)));
    }

    // 작성자 ID 확인
    private UUID fetchTargetUserId(TargetType type, UUID targetId) {
        try {
            return switch (type) {
                case POST -> communityClient.getPostWriter(targetId).writerId();
                case COMMENT -> communityClient.getCommentWriter(targetId).writerId();
                case CHAT -> chatClient.getChatMessageWriter(targetId).userId();
            };
        } catch (Exception e) {
            throw new ReportException(ReportErrorCode.REPORT_NOT_FOUND);
        }
    }
}
