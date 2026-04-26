package com.ojosama.eventservice.event.domain.model.vo;

import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventFee {
    private Integer minFee;
    private Integer maxFee;

    public EventFee(Integer minFee, Integer maxFee) {
        validateFee(minFee, maxFee);
        this.minFee = minFee;
        this.maxFee = maxFee;
    }

    private void validateFee(Integer minFee, Integer maxFee) {
        if (minFee == null || maxFee == null) {
            throw new EventException(EventErrorCode.EVENT_INVALID_FEE);
        }
        if (minFee < 0 || maxFee < 0) {
            throw new EventException(EventErrorCode.EVENT_INVALID_FEE);
        }
        if (minFee > maxFee) {
            throw new EventException(EventErrorCode.EVENT_INVALID_FEE);
        }
    }

    public boolean isInFeeRange(Integer fee) {
        return fee >= minFee && fee <= maxFee;
    }
}