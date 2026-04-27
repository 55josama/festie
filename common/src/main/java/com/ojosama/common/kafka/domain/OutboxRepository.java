package com.ojosama.common.kafka.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxRepository extends JpaRepository<OutboxMessage, UUID> {

//     발행 대기 중인 메시지를 오래된 순으로 가져온다.
//     폴러가 일정 주기마다 호출.
    @Query("SELECT o FROM OutboxMessage o "
            + "WHERE o.status = com.ojosama.common.kafka.domain.OutboxStatus.PENDING "
            + "ORDER BY o.createdAt ASC")
    List<OutboxMessage> findPending(Pageable pageable);

//     오래 전에 SENT 처리된 행을 정리할 때 사용. 별도 배치로 호출.
    @Modifying
    @Query("DELETE FROM OutboxMessage o "
            + "WHERE o.status = com.ojosama.common.kafka.domain.OutboxStatus.SENT "
            + "AND o.sentAt < :before")
    int purgeSentBefore(@Param("before") LocalDateTime before);
}
