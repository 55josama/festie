package com.ojosama.chatservice.infrastructure.persistence;

import com.ojosama.chatservice.domain.model.Message;
import com.ojosama.chatservice.domain.model.MessageStatus;
import com.ojosama.chatservice.domain.model.EventCategory;
import java.util.List;
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
    public Slice<Message> findByChatRoomIdAndStatuses(UUID chatRoomId, List<MessageStatus> statuses, Pageable pageable) {
        return messageJpaRepository.findByChatRoomIdAndStatusInOrderByCreatedAtDescIdDesc(chatRoomId, statuses, pageable);
    }

    @Override
    public Slice<Message> findByStatusesAndCategory(List<MessageStatus> statuses, EventCategory category, Pageable pageable) {
        return messageJpaRepository.findByStatusInAndCategoryOrderByCreatedAtDescIdDesc(statuses, category, pageable);
    }
}
