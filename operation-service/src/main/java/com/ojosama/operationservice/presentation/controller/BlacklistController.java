package com.ojosama.operationservice.presentation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.operationservice.application.dto.query.ListBlacklistQuery;
import com.ojosama.operationservice.application.service.BlacklistService;
import com.ojosama.operationservice.presentation.dto.CreateBlacklistRequest;
import com.ojosama.operationservice.presentation.dto.FindBlacklistResponse;
import com.ojosama.operationservice.presentation.dto.ListBlacklistResponse;
import com.ojosama.operationservice.presentation.dto.UpdateBlacklistRequest;
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
@RequestMapping("/v1/blacklists")
@RequiredArgsConstructor
public class BlacklistController {
    private final BlacklistService blacklistService;

    // 블랙리스트 수동 등록
    @PostMapping
    public ResponseEntity<ApiResponse<FindBlacklistResponse>> createBlacklist(
            @Valid @RequestBody CreateBlacklistRequest request) {

        var result = blacklistService.createBlacklistManual(request.toCommand());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(FindBlacklistResponse.from(result)));
    }

    // 블랙리스트 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ListBlacklistResponse>>> getBlacklists(
            @ModelAttribute ListBlacklistQuery query,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ListBlacklistResponse> response = blacklistService.getBlacklists(query, pageable)
                .map(ListBlacklistResponse::from);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 블랙리스트 해제
    @PatchMapping("/{blacklistId}/status")
    public ResponseEntity<ApiResponse<FindBlacklistResponse>> releaseBlacklist(
            @PathVariable UUID blacklistId,
            @Valid @RequestBody UpdateBlacklistRequest request) {

        var result = blacklistService.releaseBlacklist(blacklistId, request.toCommand());
        return ResponseEntity.ok(ApiResponse.success(FindBlacklistResponse.from(result)));
    }
}
