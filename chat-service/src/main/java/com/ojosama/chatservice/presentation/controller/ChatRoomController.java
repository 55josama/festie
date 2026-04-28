package com.ojosama.chatservice.presentation.controller;

import com.ojosama.chatservice.application.dto.command.ChangeChatRoomStatusCommand;
import com.ojosama.chatservice.application.dto.command.ChatRoomStatusAction;
import com.ojosama.chatservice.application.dto.command.CreateChatRoomCommand;
import com.ojosama.chatservice.application.dto.query.FindChatRoomByEventIdQuery;
import com.ojosama.chatservice.application.dto.query.FindChatRoomQuery;
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
@RequestMapping("/v1/chat/rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    // 관리자의 채팅방 강제 생성
    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createChatRoom(
            // TODO: 추후 인증/인가 추가
            //  - 매니저/관리자만 ChatRoom 생성 가능
            //  - @PreAuthorize("hasAnyRole('ADMIN', 'FESTIVAL_MANAGER','CONCERT_MANAGER','FANMETTING_MANAGER','POPUPSTORE_MANAGER',)")
            //  - @AuthenticationPrincipal String adminId,
            //  - @RequestHeader(value = "X-User-Role", required = true) String role,
            @RequestHeader("X-User-Id") UUID adminId,
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

    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getChatRoom(
            // TODO: 추후 인증/인가 추가
            //  - 매니저/관리자의 ChatRoomId로 채팅방 상세 조회
            //  - @PreAuthorize("hasAnyRole('ADMIN', 'FESTIVAL_MANAGER','CONCERT_MANAGER','FANMETTING_MANAGER','POPUPSTORE_MANAGER',)")
            //  - @AuthenticationPrincipal String adminId,
            //  - @RequestHeader(value = "X-User-Role", required = true) String role,
            @PathVariable UUID chatRoomId
    ) {
        ChatRoomResult result = chatRoomService.getChatRoom(new FindChatRoomQuery(chatRoomId));
        return ResponseEntity.ok(ApiResponse.success(ChatRoomResponse.from(result)));
    }

    @GetMapping("/event")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getChatRoomByEventId(
            @RequestParam UUID eventId
    ) {
        ChatRoomResult result = chatRoomService.getChatRoomByEventId(new FindChatRoomByEventIdQuery(eventId));
        return ResponseEntity.ok(ApiResponse.success(ChatRoomResponse.from(result)));
    }

    @PatchMapping("/{chatRoomId}/status")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> changeChatRoomStatus(
            // TODO: 추후 인증/인가 추가
            //  - 매니저/관리자만 채팅방 상태 변경 가능
            //  - @PreAuthorize("hasAnyRole('ADMIN', 'FESTIVAL_MANAGER','CONCERT_MANAGER','FANMETTING_MANAGER','POPUPSTORE_MANAGER',)")
            //  - @AuthenticationPrincipal String adminId,
            //  - @RequestHeader(value = "X-User-Role", required = true) String role,
            @RequestHeader("X-User-Id") UUID adminId,
            @PathVariable UUID chatRoomId,
            @Valid @RequestBody ChangeChatRoomStatusRequest request
    ) {
        ChatRoomResult result = chatRoomService.changeChatRoomStatus(
                new ChangeChatRoomStatusCommand(
                        chatRoomId,
                        toAction(request.action()),
                        adminId
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
