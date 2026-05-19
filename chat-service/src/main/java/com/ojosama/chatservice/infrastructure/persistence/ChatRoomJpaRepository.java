package com.ojosama.chatservice.infrastructure.persistence;

import com.ojosama.chatservice.domain.model.ChatRoom;
import com.ojosama.chatservice.domain.model.ChatRoomStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoom, UUID> {

    Optional<ChatRoom> findByEventId(UUID eventId);

    List<ChatRoom> findAllByStatus(ChatRoomStatus status);

    @Query(
            value = """
                    SELECT c
                    FROM ChatRoom c
                    WHERE (:status IS NULL OR c.status = :status)
                      AND (:scheduledOpenAtFrom IS NULL OR c.schedule.scheduledOpenAt >= :scheduledOpenAtFrom)
                      AND (:scheduledOpenAtTo IS NULL OR c.schedule.scheduledOpenAt <= :scheduledOpenAtTo)
                    """,
            countQuery = """
                    SELECT COUNT(c)
                    FROM ChatRoom c
                    WHERE (:status IS NULL OR c.status = :status)
                      AND (:scheduledOpenAtFrom IS NULL OR c.schedule.scheduledOpenAt >= :scheduledOpenAtFrom)
                      AND (:scheduledOpenAtTo IS NULL OR c.schedule.scheduledOpenAt <= :scheduledOpenAtTo)
                    """
    )
    Page<ChatRoom> findAllFiltered(
            @Param("status") ChatRoomStatus status,
            @Param("scheduledOpenAtFrom") LocalDateTime scheduledOpenAtFrom,
            @Param("scheduledOpenAtTo") LocalDateTime scheduledOpenAtTo,
            Pageable pageable
    );

    @Query("SELECT c FROM ChatRoom c WHERE c.status = 'SCHEDULED' AND c.schedule.scheduledOpenAt <= :now")
    List<ChatRoom> findScheduledToOpen(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM ChatRoom c WHERE c.status = 'OPEN' AND c.schedule.scheduledCloseAt <= :now")
    List<ChatRoom> findScheduledToClose(@Param("now") LocalDateTime now);

}
