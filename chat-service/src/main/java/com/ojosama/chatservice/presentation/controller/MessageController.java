package com.ojosama.chatservice.presentation.controller;

import com.ojosama.chatservice.application.dto.command.CreateMessageCommand;
import com.ojosama.chatservice.application.dto.command.DeleteMessageCommand;
import com.ojosama.chatservice.application.dto.query.FindMessagesByChatRoomQuery;
import com.ojosama.chatservice.application.dto.result.MessageResult;
import com.ojosama.chatservice.application.dto.result.MessageSliceResult;
import com.ojosama.chatservice.application.service.MessageService;
import com.ojosama.chatservice.presentation.dto.request.CreateMessageRequest;
import com.ojosama.chatservice.presentation.dto.response.MessageResponse;
import com.ojosama.chatservice.presentation.dto.response.MessageSliceResponse;
import com.ojosama.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "메시지 API", description = "채팅 메시지 조회, 작성, 삭제")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/chat")
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "메시지 전송",
            description = "채팅방에 메시지를 전송합니다.")
    @PostMapping("/rooms/{chatRoomId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MessageResponse>> createMessage(
            @PathVariable UUID chatRoomId,
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreateMessageRequest request
    ) {
        MessageResult result = messageService.createMessage(
                new CreateMessageCommand(chatRoomId, UUID.fromString(userId), request.content())
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(MessageResponse.from(result)));
    }

    @Operation(summary = "채팅방 메시지 조회",
            description = "채팅방 메시지를 페이지 단위로 조회합니다.(스크롤)")
    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<ApiResponse<MessageSliceResponse>> getMessagesByChatRoom(
            @PathVariable UUID chatRoomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        MessageSliceResult result = messageService.getMessagesByChatRoom(
                new FindMessagesByChatRoomQuery(chatRoomId, page, size)
        );
        return ResponseEntity.ok(ApiResponse.success(MessageSliceResponse.from(result)));
    }

    @Operation(summary = "메시지 삭제",
            description = "본인이 작성한 메시지를 삭제합니다.")
    @DeleteMapping("/messages/{messageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @PathVariable UUID messageId,
            @Parameter(hidden = true) @AuthenticationPrincipal String userId
    ) {
        messageService.deleteMessage(new DeleteMessageCommand(messageId, UUID.fromString(userId)));
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
