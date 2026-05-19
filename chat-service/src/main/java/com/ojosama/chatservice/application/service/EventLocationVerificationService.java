package com.ojosama.chatservice.application.service;

import com.ojosama.chatservice.application.dto.command.VerifyEventLocationCommand;
import com.ojosama.chatservice.application.dto.result.EventLocationVerificationResult;
import com.ojosama.chatservice.domain.exception.ChatErrorCode;
import com.ojosama.chatservice.domain.exception.ChatException;
import com.ojosama.chatservice.infrastructure.client.EventClient;
import com.ojosama.chatservice.infrastructure.client.dto.InternalEventLocationResponse;
import feign.FeignException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventLocationVerificationService {

    private static final double METERS_PER_LATITUDE_DEGREE = 111_320.0;
    private static final Duration LOCATION_VERIFICATION_TTL = Duration.ofHours(24);

    private final EventClient eventClient;
    private final ChatRoomLocationVerificationTracker locationVerificationTracker;

    // 사용자의 위치 정보를 받아서 거리 검증
    public EventLocationVerificationResult verify(VerifyEventLocationCommand command) {
        validate(command);

        InternalEventLocationResponse event = loadEventLocation(command.eventId());
        // 이벤트 반경 정보 없으면 1000m
        int radiusMeters = event.radiusMeters() != null ? event.radiusMeters() : 1000;
        boolean nearEvent = isNearEvent(
                command.currentLatitude(),
                command.currentLongitude(),
                event.latitude(),
                event.longitude(),
                radiusMeters
        );
        if (nearEvent) {
            locationVerificationTracker.markVerified(event.eventId(), command.userId(), LOCATION_VERIFICATION_TTL);
        }
        return new EventLocationVerificationResult(
                event.eventId(),
                nearEvent
        );
    }

    private void validate(VerifyEventLocationCommand command) {
        if (command == null || command.eventId() == null || command.userId() == null
                || command.currentLatitude() == null || command.currentLongitude() == null) {
            throw new ChatException(ChatErrorCode.INVALID_LOCATION_REQUEST);
        }
    }

    private InternalEventLocationResponse loadEventLocation(UUID eventId) {
        try {
            InternalEventLocationResponse event = eventClient.getInternalEventLocation(eventId);
            if (event == null || event.eventId() == null || event.latitude() == null || event.longitude() == null) {
                throw new ChatException(ChatErrorCode.EVENT_LOCATION_LOOKUP_FAILED);
            }
            return event;
        } catch (FeignException.NotFound e) {
            throw new ChatException(ChatErrorCode.EVENT_LOCATION_NOT_FOUND);
        } catch (FeignException e) {
            throw new ChatException(ChatErrorCode.EVENT_LOCATION_LOOKUP_FAILED);
        }
    }

    private boolean isNearEvent(
            BigDecimal currentLatitude,
            BigDecimal currentLongitude,
            BigDecimal eventLatitude,
            BigDecimal eventLongitude,
            int radiusMeters
    ) {
        // 위도/경도는 "미터"가 아니어서 그대로 비교할 수 없다.
        // 대신 위도 1도는 대략 111.32km라는 점을 이용해,
        // 위도 차이와 경도 차이를 각각 미터로 바꾼 뒤 단순한 직선 거리로 근사한다.
        double userLat = currentLatitude.doubleValue();
        double userLng = currentLongitude.doubleValue();
        double eventLat = eventLatitude.doubleValue();
        double eventLng = eventLongitude.doubleValue();

        double latDiffMeters = Math.abs(userLat - eventLat) * METERS_PER_LATITUDE_DEGREE;
        double lngMetersPerDegree = METERS_PER_LATITUDE_DEGREE * Math.cos(Math.toRadians(eventLat));
        double lngDiffMeters = Math.abs(userLng - eventLng) * lngMetersPerDegree;

        double distanceMeters = Math.sqrt(latDiffMeters * latDiffMeters + lngDiffMeters * lngDiffMeters);
        return distanceMeters <= radiusMeters; // true or false
    }

}
