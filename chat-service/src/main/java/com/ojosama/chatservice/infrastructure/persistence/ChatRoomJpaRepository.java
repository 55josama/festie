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
                    SELECT * FROM chat_schema.p_chat_room cr
                    WHERE (CAST(:status AS TEXT) IS NULL OR cr.status = CAST(:status AS TEXT))
                      AND (CAST(:scheduledOpenAtFrom AS TIMESTAMP) IS NULL OR cr.scheduled_open_at >= :scheduledOpenAtFrom)
                      AND (CAST(:scheduledOpenAtTo AS TIMESTAMP) IS NULL OR cr.scheduled_open_at <= :scheduledOpenAtTo)
                    """,
            countQuery = """
                    SELECT COUNT(*) FROM chat_schema.p_chat_room cr
                    WHERE (CAST(:status AS TEXT) IS NULL OR cr.status = CAST(:status AS TEXT))
                      AND (CAST(:scheduledOpenAtFrom AS TIMESTAMP) IS NULL OR cr.scheduled_open_at >= :scheduledOpenAtFrom)
                      AND (CAST(:scheduledOpenAtTo AS TIMESTAMP) IS NULL OR cr.scheduled_open_at <= :scheduledOpenAtTo)
                    """,
            nativeQuery = true
    )
    Page<ChatRoom> findAllFiltered(
            @Param("status") String status,
            @Param("scheduledOpenAtFrom") LocalDateTime scheduledOpenAtFrom,
            @Param("scheduledOpenAtTo") LocalDateTime scheduledOpenAtTo,
            Pageable pageable
    );

    @Query("SELECT c FROM ChatRoom c WHERE c.status = 'SCHEDULED' AND c.schedule.scheduledOpenAt <= :now")
    List<ChatRoom> findScheduledToOpen(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM ChatRoom c WHERE c.status = 'OPEN' AND c.schedule.scheduledCloseAt <= :now")
    List<ChatRoom> findScheduledToClose(@Param("now") LocalDateTime now);

}
