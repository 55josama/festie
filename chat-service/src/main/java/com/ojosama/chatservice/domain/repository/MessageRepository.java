package com.ojosama.chatservice.domain.repository;

import com.ojosama.chatservice.domain.model.Message;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface MessageRepository {

    Message save(Message message);

    Optional<Message> findById(UUID id);

    // 채팅 히스토리 조회 (무한스크롤 - cursor 기반)
    Slice<Message> findByChatRoomId(UUID chatRoomId, Pageable pageable);

}
