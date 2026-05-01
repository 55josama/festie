package com.ojosama.chatservice.infrastructure.persistence;

import com.ojosama.chatservice.domain.model.Message;
import com.ojosama.chatservice.domain.model.MessageStatus;
import com.ojosama.chatservice.domain.model.EventCategory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageJpaRepository extends JpaRepository<Message, UUID> {
    Slice<Message> findByChatRoomIdAndStatusInOrderByCreatedAtDescIdDesc(
            UUID chatRoomId,
            List<MessageStatus> statuses,
            Pageable pageable
    );

    @Query("""
            select m
            from Message m
            join ChatRoom c on m.chatRoomId = c.id
            where m.status in :statuses
              and (:category is null or c.category = :category)
            order by m.createdAt desc, m.id desc
            """)
    Slice<Message> findByStatusInAndCategoryOrderByCreatedAtDescIdDesc(
            @Param("statuses") List<MessageStatus> statuses,
            @Param("category") EventCategory category,
            Pageable pageable
    );
}
