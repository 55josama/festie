package com.ojosama.common.kafka.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboxRepository extends JpaRepository<InboxMessage, UUID> {

    boolean existsByMessageKeyAndConsumerGroup(UUID messageKey, String consumerGroup);

}
