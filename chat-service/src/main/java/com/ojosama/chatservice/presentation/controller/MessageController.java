package com.ojosama.chatservice.presentation.controller;

import com.ojosama.chatservice.application.dto.command.ChangeMessageStatusCommand;
import com.ojosama.chatservice.application.dto.command.CreateMessageCommand;
import com.ojosama.chatservice.application.dto.command.DeleteMessageCommand;
import com.ojosama.chatservice.application.dto.query.FindMessageQuery;
import com.ojosama.chatservice.application.dto.query.FindMessagesByChatRoomQuery;
import com.ojosama.chatservice.application.dto.result.MessageResult;
import com.ojosama.chatservice.application.dto.result.MessageSliceResult;
import com.ojosama.chatservice.application.service.MessageService;
import com.ojosama.chatservice.domain.model.MessageStatus;
import com.ojosama.chatservice.presentation.dto.request.ChangeMessageStatusRequest;
import com.ojosama.chatservice.presentation.dto.request.CreateMessageRequest;
import com.ojosama.chatservice.presentation.dto.response.ChangeMessageStatusResponse;
import com.ojosama.chatservice.presentation.dto.response.MessageResponse;
import com.ojosama.chatservice.presentation.dto.response.MessageSliceResponse;
import com.ojosama.common.exception.CommonErrorCode;
import com.ojosama.chatservice.domain.exception.ChatException;
import com.ojosama.common.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/chat")
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> createMessage(
            @PathVariable UUID chatRoomId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Nickname") String writerNickname,
            @Valid @RequestBody CreateMessageRequest request
    ) {
        MessageResult result = messageService.createMessage(
                new CreateMessageCommand(chatRoomId, userId, writerNickname, request.content())
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(MessageResponse.from(result)));
    }

    @GetMapping("/messages/{messageId}")
    public ResponseEntity<ApiResponse<MessageResponse>> getMessage(@PathVariable UUID messageId) {
        MessageResult result = messageService.getMessage(new FindMessageQuery(messageId));
        return ResponseEntity.ok(ApiResponse.success(MessageResponse.from(result)));
    }

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

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @PathVariable UUID messageId,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        messageService.deleteMessage(new DeleteMessageCommand(messageId, userId));
        return ResponseEntity.ok(ApiResponse.deleted());
    }

    @PatchMapping("/messages/{messageId}/status")
    public ResponseEntity<ApiResponse<ChangeMessageStatusResponse>> changeMessageStatus(
            @PathVariable UUID messageId,
            @RequestHeader("X-User-Id") UUID adminId,
            @Valid @RequestBody ChangeMessageStatusRequest request
    ) {
        if (request.status() == MessageStatus.DELETED) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        MessageResult result = messageService.changeMessageStatus(
                new ChangeMessageStatusCommand(messageId, adminId, request.status())
        );
        return ResponseEntity.ok(ApiResponse.success(ChangeMessageStatusResponse.from(result, adminId)));
    }
}
