package com.ojosama.eventservice.event.infrastructure.persistence;

import com.ojosama.eventservice.event.domain.model.Event;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEventRepository extends JpaRepository<Event, UUID> {

    Optional<Event> findByIdAndDeletedAtIsNull(UUID id);

    List<Event> findAllByIdInAndDeletedAtIsNull(Collection<UUID> ids);
}
