package com.ojosama.chatservice.presentation.controller;

import com.ojosama.chatservice.application.dto.query.FindChatRoomByEventIdQuery;
import com.ojosama.chatservice.application.dto.query.FindChatRoomQuery;
import com.ojosama.chatservice.application.dto.result.ChatRoomResult;
import com.ojosama.chatservice.application.dto.result.PopularChatRoomResult;
import com.ojosama.chatservice.application.service.ChatRoomService;
import com.ojosama.chatservice.application.service.PopularChatRoomQueryService;
import com.ojosama.chatservice.presentation.dto.response.ChatRoomResponse;
import com.ojosama.chatservice.presentation.dto.response.PopularChatRoomResponse;
import com.ojosama.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "채팅방 API", description = "채팅방 조회 및 인기 채팅방 조회")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/chat/rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final PopularChatRoomQueryService popularChatRoomQueryService;

    @Operation(summary = "채팅방 단건 조회",
            description = "채팅방 ID로 채팅방 상세를 조회합니다.")
    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getChatRoom(
            @PathVariable UUID chatRoomId
    ) {
        ChatRoomResult result = chatRoomService.getChatRoom(new FindChatRoomQuery(chatRoomId));
        return ResponseEntity.ok(ApiResponse.success(ChatRoomResponse.from(result)));
    }

    @Operation(summary = "행사 ID로 채팅방 조회",
            description = "행사 ID로 연결된 채팅방을 조회합니다.")
    @GetMapping("/event")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getChatRoomByEventId(
            @RequestParam UUID eventId
    ) {
        ChatRoomResult result = chatRoomService.getChatRoomByEventId(new FindChatRoomByEventIdQuery(eventId));
        return ResponseEntity.ok(ApiResponse.success(ChatRoomResponse.from(result)));
    }

    @Operation(summary = "인기 채팅방 조회",
            description = "현재 인기 채팅방 목록을 조회합니다.")
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<PopularChatRoomResponse>>> getPopularChatRooms(
            @RequestParam(defaultValue = "3") int limit
    ) {
        List<PopularChatRoomResult> results = popularChatRoomQueryService.getPopularChatRooms(limit);
        return ResponseEntity.ok(ApiResponse.success(
                results.stream()
                        .map(PopularChatRoomResponse::from)
                        .toList()
        ));
    }
}
