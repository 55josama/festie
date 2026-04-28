package com.ojosama.report.presentation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.report.application.dto.query.ListReportQuery;
import com.ojosama.report.application.service.ReportService;
import com.ojosama.report.domain.exception.ReportErrorCode;
import com.ojosama.report.domain.exception.ReportException;
import com.ojosama.report.domain.model.enums.ReportTargetType;
import com.ojosama.report.domain.model.enums.ReporterType;
import com.ojosama.report.infrastructure.client.ChatClient;
import com.ojosama.report.infrastructure.client.CommunityClient;
import com.ojosama.report.presentation.dto.CreateReportRequest;
import com.ojosama.report.presentation.dto.FindReportResponse;
import com.ojosama.report.presentation.dto.ListReportResponse;
import com.ojosama.report.presentation.dto.UpdateReportRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final CommunityClient communityClient;
    private final ChatClient chatClient;

    // TODO: Security 연동 후 @AuthenticationPrincipal 적용
    private static final UUID DUMMY_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    // 신고 생성
    @PostMapping
    public ResponseEntity<ApiResponse<FindReportResponse>> createReport(
            @Valid @RequestBody CreateReportRequest request) {

        UUID targetUserId = fetchTargetUserId(request.targetType(), request.targetId());

        var command = request.toCommand(DUMMY_USER_ID, targetUserId);
        var result = reportService.createReport(command, ReporterType.USER);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(FindReportResponse.from(result)));
    }

    // 신고 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ListReportResponse>>> getReportList(
            @ModelAttribute ListReportQuery query,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ListReportResponse> response = reportService.getReportList(query, pageable)
                .map(ListReportResponse::from);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 신고 상세 조회
    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<FindReportResponse>> getReportInfo(
            @PathVariable UUID reportId) {

        var result = reportService.getReportInfo(reportId);
        return ResponseEntity.ok(ApiResponse.success(FindReportResponse.from(result)));
    }

    // 신고 처리
    @PatchMapping("/{reportId}/status")
    public ResponseEntity<ApiResponse<FindReportResponse>> updateReportStatus(
            @PathVariable UUID reportId,
            @Valid @RequestBody UpdateReportRequest request) {

        var result = reportService.updateReport(reportId, request.toCommand());
        return ResponseEntity.ok(ApiResponse.success(FindReportResponse.from(result)));
    }

    // 작성자 ID 확인
    private UUID fetchTargetUserId(ReportTargetType type, UUID targetId) {
        try {
            return switch (type) {
                case POST -> communityClient.getPostWriter(targetId).userId();
                case COMMENT -> communityClient.getCommentWriter(targetId).userId();
                case CHAT -> chatClient.getChatMessageWriter(targetId).userId();
            };
        } catch (Exception e) {
            throw new ReportException(ReportErrorCode.REPORT_NOT_FOUND);
        }
    }
}
