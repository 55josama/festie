package com.ojosama.userservice.infrastructure.messaging.kafka.consumer;

import com.ojosama.userservice.domain.model.User;
import com.ojosama.userservice.domain.model.UserStatus;
import com.ojosama.userservice.domain.repository.UserRepository;
import com.ojosama.userservice.infrastructure.messaging.kafka.dto.UserBlacklistStatusEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserBlacklistStatusEventConsumer {

    private final UserRepository userRepository;

    @Transactional
    @KafkaListener(
            topics = "${kafka.topic.user-blacklist-status}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(UserBlacklistStatusEvent event) {
        User user = userRepository.findByIdAndDeletedAtIsNull(event.userId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.changeStatus(toUserStatus(event.status()));
    }

    private UserStatus toUserStatus(String blacklistStatus) {
        return switch (blacklistStatus) {
            case "ACTIVE" -> UserStatus.BLOCKED;
            case "INACTIVE" -> UserStatus.ACTIVE;
            default -> throw new IllegalArgumentException("알 수 없는 블랙리스트 상태입니다: " + blacklistStatus);
        };
    }
}