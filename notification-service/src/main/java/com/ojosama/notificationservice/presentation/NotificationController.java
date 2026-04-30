package com.ojosama.notificationservice.presentation;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.notificationservice.application.NotificationService;
import com.ojosama.notificationservice.domain.model.notification.Notification;
import com.ojosama.notificationservice.presentation.dto.NotificationResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotification(
            @RequestHeader("X-User-Id") UUID receiverId) {
        List<NotificationResponse> notificationResponseList = notificationService.selectAll(receiverId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(notificationResponseList));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> markAllAsRead(
            @RequestHeader("X-User-Id") UUID receiverId) {
        List<NotificationResponse> notificationList = notificationService.markAllAsRead(receiverId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(notificationList));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Notification>> deleteNotification(@PathVariable UUID notificationId,
                                                                        @RequestHeader("X-User-Id") UUID receiverId) {
        notificationService.deleteNotification(receiverId, notificationId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.deleted());
    }
}
