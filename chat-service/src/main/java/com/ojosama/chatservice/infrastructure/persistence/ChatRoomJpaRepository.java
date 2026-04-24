package com.ojosama.chatservice.infrastructure.persistence;

import com.ojosama.chatservice.domain.model.ChatRoom;
import com.ojosama.chatservice.domain.model.ChatRoomStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoom, UUID> {

    Optional<ChatRoom> findByEventId(UUID eventId);

    List<ChatRoom> findAllByStatus(ChatRoomStatus status);

    @Query("SELECT c FROM ChatRoom c WHERE c.status = 'SCHEDULED' AND c.scheduledOpenAt <= :now")
    List<ChatRoom> findScheduledToOpen(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM ChatRoom c WHERE c.status = 'OPEN' AND c.scheduledCloseAt <= :now")
    List<ChatRoom> findScheduledToClose(@Param("now") LocalDateTime now);

}
