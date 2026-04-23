package com.ojosama.chatservice.infrastructure.persistence;

import com.ojosama.chatservice.domain.model.Message;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageJpaRepository extends JpaRepository<Message, UUID> {
    Slice<Message> findByChatRoomIdOrderByCreatedAtDesc(UUID chatRoomId, Pageable pageable);
}