package com.ojosama.calendarservice.calendar.infrastructure.persistence;

import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCalendarRepository extends JpaRepository<Calendar, UUID> {

    Optional<Calendar> findByIdAndDeletedAtIsNull(UUID id);

    Page<Calendar> findByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    Page<Calendar> findByUserIdAndEventDateBetweenAndDeletedAtIsNull(
            UUID userId, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
