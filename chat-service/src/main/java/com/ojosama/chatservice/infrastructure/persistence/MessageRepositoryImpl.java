package com.ojosama.chatservice.infrastructure.persistence;

import com.ojosama.chatservice.domain.model.Message;
import com.ojosama.chatservice.domain.model.MessageStatus;
import com.ojosama.chatservice.domain.repository.MessageRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MessageRepositoryImpl implements MessageRepository {

    private final MessageJpaRepository messageJpaRepository;

    @Override
    public Message save(Message message) {
        return messageJpaRepository.save(message);
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return messageJpaRepository.findById(id);
    }

    @Override
    public Slice<Message> findByChatRoomIdAndStatus(UUID chatRoomId, MessageStatus status, Pageable pageable) {
        return messageJpaRepository.findByChatRoomIdAndStatusOrderByCreatedAtDescIdDesc(chatRoomId, status, pageable);
    }
}
