package com.ojosama.eventservice.event.infrastructure.persistence;

import com.ojosama.eventservice.event.domain.model.Event;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEventRepository extends JpaRepository<Event, UUID> {

}
