package com.ojosama.chatservice.infrastructure.persistence;

import com.ojosama.chatservice.domain.model.ChatRoom;
import com.ojosama.chatservice.domain.model.ChatRoomStatus;
import com.ojosama.chatservice.domain.repository.ChatRoomRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepository {

    private final ChatRoomJpaRepository ChatRoomJpaRepository;

    @Override
    public ChatRoom save(ChatRoom chatRoom) {
        return ChatRoomJpaRepository.save(chatRoom);
    }

    @Override
    public Optional<ChatRoom> findById(UUID id) {
        return ChatRoomJpaRepository.findById(id);
    }

    @Override
    public Optional<ChatRoom> findByEventId(UUID eventId) {
        return ChatRoomJpaRepository.findByEventId(eventId);
    }

    @Override
    public Page<ChatRoom> findAll(Pageable pageable) {
        return ChatRoomJpaRepository.findAll(pageable);
    }

    @Override
    public List<ChatRoom> findAllByIds(Collection<UUID> ids) {
        return ChatRoomJpaRepository.findAllById(ids);
    }

    @Override
    public List<ChatRoom> findAllByStatus(ChatRoomStatus status) {
        return ChatRoomJpaRepository.findAllByStatus(status);
    }

    @Override
    public Page<ChatRoom> findAllFiltered(ChatRoomStatus status, LocalDateTime scheduledOpenAtFrom, LocalDateTime scheduledOpenAtTo, Pageable pageable) {
        return ChatRoomJpaRepository.findAllFiltered(
                status == null ? null : status.name(),
                scheduledOpenAtFrom,
                scheduledOpenAtTo,
                pageable
        );
    }

    @Override
    public List<ChatRoom> findScheduledToOpen(LocalDateTime now) {
        return ChatRoomJpaRepository.findScheduledToOpen(now);
    }

    @Override
    public List<ChatRoom> findScheduledToClose(LocalDateTime now) {
        return ChatRoomJpaRepository.findScheduledToClose(now);
    }
}
