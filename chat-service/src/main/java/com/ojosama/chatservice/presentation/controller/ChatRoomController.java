package com.ojosama.chatservice.presentation.controller;

import com.ojosama.chatservice.application.dto.query.FindChatRoomByEventIdQuery;
import com.ojosama.chatservice.application.dto.query.FindChatRoomQuery;
import com.ojosama.chatservice.application.dto.result.ChatRoomResult;
import com.ojosama.chatservice.application.service.ChatRoomService;
import com.ojosama.chatservice.presentation.dto.response.ChatRoomResponse;
import com.ojosama.common.response.ApiResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/chat/rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getChatRoom(
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
}
