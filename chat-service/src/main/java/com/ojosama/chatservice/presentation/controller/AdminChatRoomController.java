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
import com.ojosama.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "채팅방 관리 API", description = "관리자/매니저 전용 채팅방 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/chat/admin/rooms")
@PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER')")
public class AdminChatRoomController {

    private final ChatRoomService chatRoomService;

    @Operation(summary = "채팅방 생성",
            description = "행사에 연결된 채팅방을 생성합니다."
                    + "행사 생성을 통해 채팅방이 제대로 만들어지지 않았을경우에 강제 생성합니다.")
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

    @Operation(summary = "모든 채팅방 조회",
            description = "관리자/매니저용 채팅방을 모두 불러옵니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ChatRoomResponse>>> getAllChatRooms(
            @PageableDefault(size = 3, sort = "schedule.scheduledOpenAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.from(chatRoomService.getAllChatRooms(pageable)
                        .map(ChatRoomResponse::from))
        ));
    }

    @Operation(summary = "채팅방 상태 변경",
            description = "자동 스케쥴러에 의해 채팅방이 오픈/클로즈 되지 않았을 경우 강제 오픈/클로즈 합니다.")
    @PatchMapping("/{chatRoomId}/status")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> changeChatRoomStatus(
            @PathVariable UUID chatRoomId,
            @Parameter(hidden = true) @AuthenticationPrincipal String adminId,
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
