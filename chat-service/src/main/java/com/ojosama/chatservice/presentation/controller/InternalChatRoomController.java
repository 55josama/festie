package com.ojosama.chatservice.presentation.controller;

import com.ojosama.chatservice.application.dto.query.FindChatRoomByEventIdQuery;
import com.ojosama.chatservice.application.dto.result.ChatRoomSummaryResult;
import com.ojosama.chatservice.application.service.ChatRoomService;
import com.ojosama.chatservice.presentation.dto.response.ChatRoomSummaryResponse;
import com.ojosama.common.response.ApiResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/chat/rooms")
public class InternalChatRoomController {

    private final ChatRoomService chatRoomService;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<ChatRoomSummaryResponse>> getChatRoomSummaryByEventId(
            @PathVariable UUID eventId
    ) {
        ChatRoomSummaryResult result = chatRoomService.getChatRoomSummaryByEventId(
                new FindChatRoomByEventIdQuery(eventId)
        );
        return ResponseEntity.ok(ApiResponse.success(ChatRoomSummaryResponse.from(result)));
    }
}
