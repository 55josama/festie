package com.ojosama.calendarservice.calendar.infrastructure.persistence;

import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCalendarRepository extends JpaRepository<Calendar, UUID>, CalendarRepositoryCustom {

    Optional<Calendar> findByEventInfo_EventScheduleIdAndUserIdAndDeletedAtIsNull(UUID eventScheduleId, UUID userId);

    Optional<Calendar> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<Calendar> findAllByEventInfo_EventIdAndDeletedAtIsNull(UUID eventId);
}
