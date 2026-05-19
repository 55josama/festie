package com.ojosama.chatservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ojosama.chatservice.application.dto.command.VerifyEventLocationCommand;
import com.ojosama.chatservice.infrastructure.client.EventClient;
import com.ojosama.chatservice.infrastructure.client.dto.InternalEventLocationResponse;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventLocationVerificationServiceTest {

    @Mock
    private EventClient eventClient;

    @Mock
    private ChatRoomLocationVerificationTracker locationVerificationTracker;

    private EventLocationVerificationService service;

    @BeforeEach
    void setUp() {
        service = new EventLocationVerificationService(eventClient, locationVerificationTracker);
    }

    @Test
    void verify_shouldStoreVerifiedUserInRedis_whenNearEvent() {
        UUID eventId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID userId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

        when(eventClient.getInternalEventLocation(eventId)).thenReturn(new InternalEventLocationResponse(
                eventId,
                "테스트 행사",
                "서울",
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780),
                1000
        ));
        var result = service.verify(new VerifyEventLocationCommand(
                eventId,
                userId,
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780)
        ));
        assertThat(result.nearEvent()).isTrue();
        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(locationVerificationTracker).markVerified(eq(eventId), eq(userId), ttlCaptor.capture());
        assertThat(ttlCaptor.getValue()).isEqualTo(Duration.ofHours(24));
    }

    @Test
    void verify_shouldNotStoreAnything_whenFarFromEvent() {
        UUID eventId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID userId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

        when(eventClient.getInternalEventLocation(eventId)).thenReturn(new InternalEventLocationResponse(
                eventId,
                "테스트 행사",
                "서울",
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780),
                1000
        ));

        var result = service.verify(new VerifyEventLocationCommand(
                eventId,
                userId,
                BigDecimal.valueOf(35.1796),
                BigDecimal.valueOf(129.0756)
        ));

        assertThat(result.nearEvent()).isFalse();
        verify(locationVerificationTracker, never()).markVerified(
                any(UUID.class),
                any(UUID.class),
                any(Duration.class)
        );
    }
}
