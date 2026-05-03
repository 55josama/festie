package com.ojosama.chatservice.infrastructure.client;

import com.ojosama.chatservice.infrastructure.client.dto.InternalUserNicknameResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/internal/v1/users/{userId}/nickname")
    InternalUserNicknameResponse getInternalUserNickname(@PathVariable("userId") UUID userId);
}
