package com.ojosama.chatservice.presentation.controller;

import com.ojosama.chatservice.application.dto.command.ChangeChatRoomStatusCommand;
import com.ojosama.chatservice.application.dto.command.ChatRoomStatusAction;
import com.ojosama.chatservice.application.dto.command.CreateChatRoomCommand;
import com.ojosama.chatservice.application.dto.result.ChatRoomResult;
import com.ojosama.chatservice.application.service.ChatRoomService;
import com.ojosama.chatservice.presentation.dto.request.ChangeChatRoomStatusRequest;
import com.ojosama.chatservice.presentation.dto.request.ChatRoomStatusActionRequest;
import com.ojosama.chatservice.presentation.dto.request.CreateChatRoomRequest;
import com.ojosama.chatservice.presentation.dto.response.ChatRoomResponse;
import com.ojosama.common.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/chat/admin/rooms")
@PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER')")
public class AdminChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createChatRoom(
            @Valid @RequestBody CreateChatRoomRequest request
    ) {
        ChatRoomResult result = chatRoomService.createChatRoom(
                new CreateChatRoomCommand(
                        request.eventId(),
                        request.eventName(),
                        request.category(),
                        request.scheduledOpenAt(),
                        request.scheduledCloseAt()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(ChatRoomResponse.from(result)));
    }

    @PatchMapping("/{chatRoomId}/status")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> changeChatRoomStatus(
            @PathVariable UUID chatRoomId,
            @AuthenticationPrincipal String adminId,
            @Valid @RequestBody ChangeChatRoomStatusRequest request
    ) {
        ChatRoomResult result = chatRoomService.changeChatRoomStatus(
                new ChangeChatRoomStatusCommand(
                        chatRoomId,
                        toAction(request.action()),
                        UUID.fromString(adminId)
                )
        );
        return ResponseEntity.ok(ApiResponse.success(ChatRoomResponse.from(result)));
    }

    private ChatRoomStatusAction toAction(ChatRoomStatusActionRequest action) {
        return switch (action) {
            case FORCE_OPEN -> ChatRoomStatusAction.FORCE_OPEN;
            case FORCE_CLOSE -> ChatRoomStatusAction.FORCE_CLOSE;
        };
    }
}
