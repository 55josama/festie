package com.ojosama.eventservice.event.domain.model.vo;

import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventLocation {
    private String place;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public EventLocation(String place, BigDecimal latitude, BigDecimal longitude) {
        validatePlace(place);
        validateLatitude(latitude);
        validateLongitude(longitude);

        this.place = place;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    private void validatePlace(String place) {
        if (place == null || place.isBlank()) {
            throw new EventException(EventErrorCode.EVENT_INVALID_LOCATION);
        }
        if (place.length() > 500) {
            throw new EventException(EventErrorCode.EVENT_INVALID_LOCATION);
        }
    }

    private void validateLatitude(BigDecimal latitude) {
        if (latitude == null) {
            throw new EventException(EventErrorCode.EVENT_INVALID_LOCATION);
        }
        if (latitude.compareTo(new BigDecimal("-90")) < 0 ||
                latitude.compareTo(new BigDecimal("90")) > 0) {
            throw new EventException(EventErrorCode.INVALID_LATITUDE);
        }
    }

    private void validateLongitude(BigDecimal longitude) {
        if (longitude == null) {
            throw new EventException(EventErrorCode.EVENT_INVALID_LOCATION);
        }
        if (longitude.compareTo(new BigDecimal("-180")) < 0 ||
                longitude.compareTo(new BigDecimal("180")) > 0) {
            throw new EventException(EventErrorCode.INVALID_LONGITUDE);
        }
    }
}