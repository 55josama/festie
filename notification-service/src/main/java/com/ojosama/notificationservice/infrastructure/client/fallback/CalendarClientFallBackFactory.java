package com.ojosama.notificationservice.infrastructure.client.fallback;

import com.ojosama.common.exception.CommonErrorCode;
import com.ojosama.common.exception.CustomException;
import com.ojosama.notificationservice.domain.exception.NotificationErrorCode;
import com.ojosama.notificationservice.domain.exception.NotificationException;
import com.ojosama.notificationservice.infrastructure.client.CalendarClient;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CalendarClientFallBackFactory implements FallbackFactory<CalendarClient> {

    @Override
    public CalendarClient create(Throwable cause) {
        return calendarId -> {
            log.error("CalendarClientFallBackFactory: {}", cause.getMessage());

            if (cause instanceof FeignException.NotFound) {
                throw new NotificationException(NotificationErrorCode.NOT_FOUND_CALENDAR);
            }
            throw new CustomException(CommonErrorCode.UNEXPECTED_ERROR);
        };
    }
}
