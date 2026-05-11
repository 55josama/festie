package com.ojosama.notificationservice.presentation;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.notificationservice.application.NotificationService;
import com.ojosama.notificationservice.application.dto.result.NotificationResult;
import com.ojosama.notificationservice.infrastructure.sse.SseEmitterManager;
import com.ojosama.notificationservice.presentation.dto.NotificationResponseDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final SseEmitterManager sseEmitterManager;

    // SSE 프론트 연결
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestHeader("X-User-Id") UUID receiverId) {
        return sseEmitterManager.subscribe(receiverId);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponseDto>>> getNotification(
            @RequestHeader("X-User-Id") UUID receiverId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable) {

        Page<NotificationResult> notificationList = notificationService.selectAll(receiverId, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(notificationList.map(NotificationResponseDto::of)));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<List<NotificationResponseDto>>> markAllAsRead(
            @RequestHeader("X-User-Id") UUID receiverId) {
        List<NotificationResult> notificationList = notificationService.markAllAsRead(receiverId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(NotificationResponseDto.of(notificationList)));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable UUID notificationId,
                                                                @RequestHeader("X-User-Id") UUID receiverId) {
        notificationService.deleteNotification(receiverId, notificationId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.deleted());
    }
}
