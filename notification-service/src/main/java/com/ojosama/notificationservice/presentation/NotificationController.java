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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotification() {

        // TODO : 유저 id 수정 필요
        UUID receiverId = UUID.fromString("bd4e3ba4-55dd-45d4-b1ca-55f38f0c4804");

        List<NotificationResponse> notificationResponseList = notificationService.selectAll(receiverId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(notificationResponseList));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> markAllAsRead() {

        // TODO : 유저 id 수정 필요
        UUID receiverId = UUID.fromString("bd4e3ba4-55dd-45d4-b1ca-55f38f0c4804");

        List<NotificationResponse> notificationList = notificationService.markAllAsRead(receiverId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(notificationList));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Notification>> deleteNotification(@PathVariable UUID notificationId) {

        // TODO : 유저 id 수정 필요
        UUID receiverId = UUID.fromString("bd4e3ba4-55dd-45d4-b1ca-55f38f0c4804");

        notificationService.deleteNotification(receiverId, notificationId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.deleted());
    }
}
