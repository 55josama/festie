package com.ojosama.blacklist.presentation.controller;

import com.ojosama.blacklist.application.dto.command.CreateBlacklistCommand;
import com.ojosama.blacklist.application.dto.result.BlacklistResult;
import com.ojosama.blacklist.domain.model.enums.BlacklistStatus;
import com.ojosama.common.response.ApiResponse;
import com.ojosama.blacklist.application.dto.query.ListBlacklistQuery;
import com.ojosama.blacklist.application.service.BlacklistService;
import com.ojosama.blacklist.presentation.dto.CreateBlacklistRequest;
import com.ojosama.blacklist.presentation.dto.FindBlacklistResponse;
import com.ojosama.blacklist.presentation.dto.ListBlacklistResponse;
import com.ojosama.blacklist.presentation.dto.UpdateBlacklistRequest;
import com.ojosama.common.response.PageResponse;
import com.ojosama.report.application.dto.result.ReportResult;
import com.ojosama.report.presentation.dto.ListReportResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/blacklists")
@RequiredArgsConstructor
public class BlacklistController {
    private final BlacklistService blacklistService;

    // 블랙리스트 수동 등록
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<FindBlacklistResponse>> createBlacklist(
            @Valid @RequestBody CreateBlacklistRequest request) {

        CreateBlacklistCommand command = request.toCommand();
        BlacklistResult result = blacklistService.createBlacklistManual(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(FindBlacklistResponse.from(result)));
    }

    // 블랙리스트 목록 조회
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ListBlacklistResponse>>> getBlacklists(
            @RequestParam(required = false) BlacklistStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        ListBlacklistQuery query = new ListBlacklistQuery(status);

        PageResponse<BlacklistResult> serviceResult = blacklistService.getBlacklists(query, pageable);

        List<ListBlacklistResponse> content = serviceResult.content().stream()
                .map(ListBlacklistResponse::from)
                .collect(Collectors.toList());

        PageResponse<ListBlacklistResponse> response = new PageResponse<>(
                content,
                serviceResult.page(),
                serviceResult.size(),
                serviceResult.totalElements(),
                serviceResult.totalPages()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 블랙리스트 해제
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{blacklistId}/status")
    public ResponseEntity<ApiResponse<FindBlacklistResponse>> releaseBlacklist(
            @PathVariable UUID blacklistId,
            @Valid @RequestBody UpdateBlacklistRequest request) {

        BlacklistResult result = blacklistService.releaseBlacklist(blacklistId, request.toCommand());
        return ResponseEntity.ok(ApiResponse.success(FindBlacklistResponse.from(result)));
    }
}
