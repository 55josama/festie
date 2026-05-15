package com.ojosama.chatservice.domain.repository;

import com.ojosama.chatservice.domain.model.ChatRoom;
import com.ojosama.chatservice.domain.model.ChatRoomStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatRoomRepository {

    ChatRoom save(ChatRoom chatRoom);

    Optional<ChatRoom> findById(UUID id);

    Optional<ChatRoom> findByEventId(UUID eventId);

    Page<ChatRoom> findAll(Pageable pageable);

    List<ChatRoom> findAllByIds(Collection<UUID> ids);

    List<ChatRoom> findAllByStatus(ChatRoomStatus status);

    // 스케줄러용: 오픈 예정인 방 조회
    List<ChatRoom> findScheduledToOpen(LocalDateTime now);

    // 스케줄러용: 종료 예정인 방 조회
    List<ChatRoom> findScheduledToClose(LocalDateTime now);

}
