package com.ojosama.chatservice.application.dto.result;

import com.ojosama.chatservice.domain.model.ChatRoom;

public record PopularChatRoomResult(
        ChatRoomResult chatRoom,
        int currentViewerCount
) {
    public static PopularChatRoomResult from(ChatRoom chatRoom, int currentViewerCount) {
        return new PopularChatRoomResult(
                ChatRoomResult.from(chatRoom),
                currentViewerCount
        );
    }
}
