package com.ojosama.chatservice.presentation.controller;

import com.ojosama.chatservice.application.dto.command.ChangeMessageStatusCommand;
import com.ojosama.chatservice.application.dto.query.FindAdminMessagesQuery;
import com.ojosama.chatservice.application.dto.query.FindMessageQuery;
import com.ojosama.chatservice.application.dto.result.MessageResult;
import com.ojosama.chatservice.application.service.MessageService;
import com.ojosama.chatservice.domain.model.EventCategory;
import com.ojosama.chatservice.domain.model.MessageStatus;
import com.ojosama.chatservice.presentation.dto.request.ChangeMessageStatusActionRequest;
import com.ojosama.chatservice.presentation.dto.request.ChangeMessageStatusRequest;
import com.ojosama.chatservice.presentation.dto.response.ChangeMessageStatusResponse;
import com.ojosama.chatservice.presentation.dto.response.MessageResponse;
import com.ojosama.common.response.ApiResponse;
import com.ojosama.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "메시지 관리 API", description = "관리자/매니저 전용 메시지 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/chat/admin/messages")
@PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER')")
public class AdminMessageController {

    private final MessageService messageService;

    @Operation(summary = "관리자/매니저 모든 메시지 조회",
            description = "상태와 카테고리로 메시지를 필터링해서 모든 메시지를 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MessageResponse>>> getMessagesForAdmin(
            @RequestParam(required = false) MessageStatus status,
            @RequestParam(required = false) EventCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(
                messageService.getMessagesForAdmin(
                        new FindAdminMessagesQuery(status, category, page, size)
                ).map(message -> MessageResponse.from(message, false))
        )));
    }

    @Operation(summary = "메시지 단건 조회",
            description = "메시지 단건 상세 조회 할 수 있습니다.")
    @GetMapping("/{messageId}")
    public ResponseEntity<ApiResponse<MessageResponse>> getMessage(@PathVariable UUID messageId) {
        MessageResult result = messageService.getMessage(new FindMessageQuery(messageId));
        return ResponseEntity.ok(ApiResponse.success(MessageResponse.from(result)));
    }

    @Operation(summary = "메시지 상태 변경",
            description = "메시지 id를 통해 상태(ACTIVE/BLINDED) 처리합니다.")
    @PatchMapping("/{messageId}/status")
    public ResponseEntity<ApiResponse<ChangeMessageStatusResponse>> changeMessageStatus(
            @PathVariable UUID messageId,
            @Parameter(hidden = true) @AuthenticationPrincipal String adminId,
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
