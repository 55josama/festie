package com.ojosama.calendarservice.calendar.infrastructure.persistence;

import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaCalendarRepository extends JpaRepository<Calendar, UUID>, CalendarRepositoryCustom {

    Optional<Calendar> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<Calendar> findByEventInfo_EventIdAndDeletedAtIsNull(UUID eventId);

    @Query("select c from Calendar c where c.eventInfo.eventDate >= :start AND c.eventInfo.eventDate < :end AND c.deletedAt is null")
    List<Calendar> findByEventInfo_EventDateBetweenAndDeletedAtIsNull(@Param("start") LocalDateTime start,
                                                                      @Param("end") LocalDateTime end);

    @Query("select c from Calendar c where c.eventInfo.eventTicketingDate >= :now AND c.eventInfo.eventTicketingDate < :oneHourLater AND c.deletedAt is null ")
    List<Calendar> findByEventInfo_EventTicketingDateBetweenAndDeletedAtIsNull(@Param("now") LocalDateTime now,
                                                                               @Param("oneHourLater") LocalDateTime oneHourLater);

    Optional<Calendar> findByEventInfo_EventIdAndEventInfo_EventDateAndUserIdAndDeletedAtIsNull(UUID eventId,
                                                                                                LocalDateTime eventDate,
                                                                                                UUID userId);

}
