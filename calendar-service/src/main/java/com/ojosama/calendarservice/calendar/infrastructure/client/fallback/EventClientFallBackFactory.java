package com.ojosama.calendarservice.calendar.infrastructure.client.fallback;

import com.ojosama.calendarservice.calendar.domain.exception.CalendarErrorCode;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarException;
import com.ojosama.calendarservice.calendar.infrastructure.client.EventClient;
import com.ojosama.common.exception.CommonErrorCode;
import com.ojosama.common.exception.CustomException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EventClientFallBackFactory implements FallbackFactory<EventClient> {

    @Override
    public EventClient create(Throwable cause) {
        return eventId -> {
            log.error("EventClient FallBack : {}", cause.getMessage());

            if (cause instanceof FeignException.NotFound) {
                throw new CalendarException(CalendarErrorCode.EVENT_NOT_FOUND);
            }

            throw new CustomException(CommonErrorCode.UNEXPECTED_ERROR);
        };

        
    }
}
