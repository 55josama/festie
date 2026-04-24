package com.ojosama.eventservice.event.infrastructor.persistence;

import com.sun.jdi.request.EventRequest;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEventRequestRespository extends JpaRepository<EventRequest, UUID> {
}
