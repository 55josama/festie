package com.ojosama.chatservice.infrastructure.persistence;

import com.ojosama.chatservice.domain.model.Message;
import com.ojosama.chatservice.domain.model.MessageStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageJpaRepository extends JpaRepository<Message, UUID> {
    Slice<Message> findByChatRoomIdAndStatusInOrderByCreatedAtDescIdDesc(
            UUID chatRoomId,
            List<MessageStatus> statuses,
            Pageable pageable
    );
}
