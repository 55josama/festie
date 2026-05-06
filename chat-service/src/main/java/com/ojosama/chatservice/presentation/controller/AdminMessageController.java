package com.ojosama.chatservice.presentation.controller;

import com.ojosama.chatservice.application.dto.command.ChangeMessageStatusCommand;
import com.ojosama.chatservice.application.dto.query.FindAdminMessagesQuery;
import com.ojosama.chatservice.application.dto.query.FindMessageQuery;
import com.ojosama.chatservice.application.dto.result.MessageResult;
import com.ojosama.chatservice.application.dto.result.MessageSliceResult;
import com.ojosama.chatservice.application.service.MessageService;
import com.ojosama.chatservice.domain.model.EventCategory;
import com.ojosama.chatservice.domain.model.MessageStatus;
import com.ojosama.chatservice.presentation.dto.request.ChangeMessageStatusActionRequest;
import com.ojosama.chatservice.presentation.dto.request.ChangeMessageStatusRequest;
import com.ojosama.chatservice.presentation.dto.response.ChangeMessageStatusResponse;
import com.ojosama.chatservice.presentation.dto.response.MessageResponse;
import com.ojosama.chatservice.presentation.dto.response.MessageSliceResponse;
import com.ojosama.common.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/chat/admin/messages")
@PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER')")
public class AdminMessageController {

    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<ApiResponse<MessageSliceResponse>> getMessagesForAdmin(
            @RequestParam(required = false) MessageStatus status,
            @RequestParam(required = false) EventCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        MessageSliceResult result = messageService.getMessagesForAdmin(
                new FindAdminMessagesQuery(status, category, page, size)
        );
        return ResponseEntity.ok(ApiResponse.success(MessageSliceResponse.from(result, false)));
    }

    @GetMapping("/messages/{messageId}")
    public ResponseEntity<ApiResponse<MessageResponse>> getMessage(@PathVariable UUID messageId) {
        MessageResult result = messageService.getMessage(new FindMessageQuery(messageId));
        return ResponseEntity.ok(ApiResponse.success(MessageResponse.from(result)));
    }

    @PatchMapping("/{messageId}/status")
    public ResponseEntity<ApiResponse<ChangeMessageStatusResponse>> changeMessageStatus(
            @PathVariable UUID messageId,
            @AuthenticationPrincipal String adminId,
            @Valid @RequestBody ChangeMessageStatusRequest request
    ) {
        MessageStatus status = toMessageStatus(request.status());
        MessageResult result = messageService.changeMessageStatus(
                new ChangeMessageStatusCommand(messageId, UUID.fromString(adminId), status)
        );
        return ResponseEntity.ok(
                ApiResponse.success(ChangeMessageStatusResponse.from(result, UUID.fromString(adminId))));
    }

    private MessageStatus toMessageStatus(ChangeMessageStatusActionRequest status) {
        return switch (status) {
            case ACTIVE -> MessageStatus.ACTIVE;
            case BLINDED -> MessageStatus.BLINDED;
        };
    }
}
