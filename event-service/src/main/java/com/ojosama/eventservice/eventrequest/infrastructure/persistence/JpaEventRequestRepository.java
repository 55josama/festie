package com.ojosama.eventservice.eventrequest.infrastructure.persistence;

import com.ojosama.eventservice.eventrequest.domain.model.EventRequest;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEventRequestRepository extends JpaRepository<EventRequest, UUID> {
}
