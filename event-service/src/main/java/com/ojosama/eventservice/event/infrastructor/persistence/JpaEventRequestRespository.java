package com.ojosama.eventservice.event.infrastructor.persistence;

import com.ojosama.eventservice.event.domain.model.EventRequest;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEventRequestRespository extends JpaRepository<EventRequest, UUID> {
}
