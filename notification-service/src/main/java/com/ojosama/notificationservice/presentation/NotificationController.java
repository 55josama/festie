package com.ojosama.notificationservice.presentation;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.common.response.PageResponse;
import com.ojosama.notificationservice.application.NotificationService;
import com.ojosama.notificationservice.application.dto.result.NotificationResult;
import com.ojosama.notificationservice.infrastructure.sse.SseEmitterManager;
import com.ojosama.notificationservice.presentation.dto.NotificationResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/notifications")
@PreAuthorize("isAuthenticated()")
@Tag(name = "알림", description = "알림 관리 API")
public class NotificationController {

    private final NotificationService notificationService;
    private final SseEmitterManager sseEmitterManager;

    // SSE 프론트 연결
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal UUID receiverId) {
        return sseEmitterManager.subscribe(receiverId);
    }

    @GetMapping
    @Operation(summary = "알림 조회", description = "자신에게 온 알림을 조회할 수 있습니다.")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponseDto>>> getNotification(
            @AuthenticationPrincipal UUID receiverId,
            @PageableDefault(size = 5, sort = "createdAt", direction = Direction.DESC) Pageable pageable) {

        PageResponse<NotificationResponseDto> notificationList = PageResponse.from(
                notificationService.selectAll(receiverId, pageable).map(NotificationResponseDto::of));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(notificationList));
    }

    @PatchMapping
    @Operation(summary = "알림 읽음처리", description = "자신에게 온 알림을 읽음처리 할 수 있습니다.")
    public ResponseEntity<ApiResponse<List<NotificationResponseDto>>> markAllAsRead(
            @AuthenticationPrincipal UUID receiverId) {
        List<NotificationResult> notificationList = notificationService.markAllAsRead(receiverId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(NotificationResponseDto.of(notificationList)));
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "알림 삭제처리", description = "알림을 삭제 처리 할 수 있습니다.")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable UUID notificationId,
                                                                @AuthenticationPrincipal UUID receiverId) {
        notificationService.deleteNotification(receiverId, notificationId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.deleted());
    }
}
