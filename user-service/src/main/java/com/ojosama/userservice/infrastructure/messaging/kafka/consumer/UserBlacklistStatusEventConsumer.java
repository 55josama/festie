package com.ojosama.userservice.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import com.ojosama.userservice.domain.exception.UserErrorCode;
import com.ojosama.userservice.domain.exception.UserException;
import com.ojosama.userservice.domain.model.User;
import com.ojosama.userservice.domain.model.UserStatus;
import com.ojosama.userservice.domain.repository.UserRepository;
import com.ojosama.userservice.infrastructure.messaging.kafka.dto.UserBlacklistStatusEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class UserBlacklistStatusEventConsumer {

    private static final String CONSUMER_GROUP = "user-service-group";
    private static final String EVENT_TYPE = EventType.BLACKLIST_UPDATED.getValue();

    private final ObjectMapper objectMapper;
    private final IdempotentEventHandler idempotentHandler;
    private final UserRepository userRepository;

    @KafkaListener(
            topics = "${kafka.topic.user-blacklist-status}",
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, String> record) {
        UUID messageKey;
        UserBlacklistStatusEvent event;

        try {
            messageKey = UUID.fromString(record.key());
            event = objectMapper.readValue(record.value(), UserBlacklistStatusEvent.class);
        } catch (Exception e) {
            log.error("블랙리스트 상태 변경 이벤트 파싱 실패. key={}, value={}",
                    record.key(), record.value(), e);
            return;
        }

        idempotentHandler.handle(
                messageKey,
                CONSUMER_GROUP,
                record.topic(),
                EVENT_TYPE,
                () -> changeUserStatus(event)
        );
    }

    private void changeUserStatus(UserBlacklistStatusEvent event) {
        User user = userRepository.findByIdAndDeletedAtIsNull(event.userId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        user.changeStatus(toUserStatus(event.status()));
    }

    private UserStatus toUserStatus(String blacklistStatus) {
        if (blacklistStatus == null || blacklistStatus.isBlank()) {
            throw new UserException(UserErrorCode.INVALID_BLACKLIST_STATUS);
        }

        return switch (blacklistStatus) {
            case "ACTIVE" -> UserStatus.BLOCKED;
            case "INACTIVE" -> UserStatus.ACTIVE;
            default -> throw new UserException(UserErrorCode.INVALID_BLACKLIST_STATUS);
        };
    }
}