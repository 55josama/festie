package com.ojosama.notice.presentation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.notice.application.dto.result.NoticeResult;
import com.ojosama.notice.application.service.NoticeService;
import com.ojosama.notice.presentation.dto.CreateNoticeRequest;
import com.ojosama.notice.presentation.dto.FindNoticeResponse;
import com.ojosama.notice.presentation.dto.ListNoticeResponse;
import com.ojosama.notice.presentation.dto.UpdateNoticeRequest;
import com.ojosama.operationrequest.presentation.dto.ListOperationResponse;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/notices")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;

    // 공지사항 생성
    // TODO: 인증/인가 도입 후 @AuthenticationPrincipal로 교체 필요
    @PostMapping
    public ResponseEntity<ApiResponse<FindNoticeResponse>> createNotice(
            @Valid @RequestBody CreateNoticeRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "11111111-1111-1111-1111-111111111111") UUID adminId
    ) {
        NoticeResult result = noticeService.createNotice(request.toCommand(adminId));

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(FindNoticeResponse.from(result)));
    }

    // 공지사항 목록 조회 (최신순 정렬, 페이징)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ListNoticeResponse>>> getNoticeList(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ListNoticeResponse> response = noticeService.getNoticeList(pageable)
                .map(ListNoticeResponse::from);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 공지사항 상세 조회
    @GetMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<FindNoticeResponse>> getNotice(
            @PathVariable UUID noticeId
    ) {
        NoticeResult result = noticeService.getNotice(noticeId);

        return ResponseEntity.ok(ApiResponse.created(FindNoticeResponse.from(result)));
    }

    // 공지사항 수정
    @PatchMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<FindNoticeResponse>> updateNotice(
            @PathVariable UUID noticeId,
            @Valid @RequestBody UpdateNoticeRequest request
    ) {
        NoticeResult result = noticeService.updateNotice(noticeId, request.toCommand(noticeId));

        return ResponseEntity.ok(ApiResponse.created(FindNoticeResponse.from(result)));
    }

    // 공지사항 삭제
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(
            @PathVariable UUID noticeId
    ) {
        noticeService.deleteNotice(noticeId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
