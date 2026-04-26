package com.ojosama.eventservice.event.domain.model.vo;

import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventLocation {

    private static final BigDecimal LATITUDE_MIN = new BigDecimal("-90");
    private static final BigDecimal LATITUDE_MAX = new BigDecimal("90");
    private static final BigDecimal LONGITUDE_MIN = new BigDecimal("-180");
    private static final BigDecimal LONGITUDE_MAX = new BigDecimal("180");

    @Column(length = 500, nullable = false)
    private String place;

    @Column(precision = 10, scale = 6, nullable = false)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 6, nullable = false)
    private BigDecimal longitude;

    public EventLocation(String place, BigDecimal latitude, BigDecimal longitude) {
        validatePlace(place);
        validateLatitude(latitude);
        validateLongitude(longitude);

        this.place = place.trim();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    private void validatePlace(String place) {
        if (place == null || place.isBlank()) {
            throw new EventException(EventErrorCode.EVENT_INVALID_LOCATION);
        }
        if (place.trim().length() > 500) {
            throw new EventException(EventErrorCode.EVENT_INVALID_LOCATION);
        }
    }

    private void validateLatitude(BigDecimal latitude) {
        if (latitude == null) {
            throw new EventException(EventErrorCode.EVENT_INVALID_LOCATION);
        }
        if (latitude.compareTo(LATITUDE_MIN) < 0 || latitude.compareTo(LATITUDE_MAX) > 0) {
            throw new EventException(EventErrorCode.INVALID_LATITUDE);
        }
    }

    private void validateLongitude(BigDecimal longitude) {
        if (longitude == null) {
            throw new EventException(EventErrorCode.EVENT_INVALID_LOCATION);
        }
        if (longitude.compareTo(LONGITUDE_MIN) < 0 || longitude.compareTo(LONGITUDE_MAX) > 0) {
            throw new EventException(EventErrorCode.INVALID_LONGITUDE);
        }
    }
}
