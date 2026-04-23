package com.ojosama.chatservice.domain.repository;

import com.ojosama.chatservice.domain.model.ChatRoom;
import com.ojosama.chatservice.domain.model.ChatRoomStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository {

    ChatRoom save(ChatRoom chatRoom);

    Optional<ChatRoom> findById(UUID id);

    Optional<ChatRoom> findByEventId(UUID eventId);

    List<ChatRoom> findAllByStatus(ChatRoomStatus status);

    // 스케줄러용: 오픈 예정인 방 조회
    List<ChatRoom> findScheduledToOpen(LocalDateTime now);

    // 스케줄러용: 종료 예정인 방 조회
    List<ChatRoom> findScheduledToClose(LocalDateTime now);

}
