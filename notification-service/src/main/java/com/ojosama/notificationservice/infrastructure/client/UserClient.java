package com.ojosama.notificationservice.infrastructure.client;

import com.ojosama.notificationservice.infrastructure.client.dto.UserInfo;
import com.ojosama.notificationservice.infrastructure.client.fallback.UserClientFallBackFactory;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", fallbackFactory = UserClientFallBackFactory.class, primary = false)
public interface UserClient {

    // userEmail 벌크 조회
    @GetMapping("/internal/v1/users/emails")
    UserInfo getUserInfo(List<UUID> userIds);

    @GetMapping("/internal/v1/users/{userId}/email")
    String getUserEmail(@PathVariable("userId") UUID userId);

    // adminId
    @GetMapping("/internal/v1/users/admin")
    UUID getAdminInfo();

    // 카테고리 이름으로 아이디 조회
    @GetMapping("/internal/v1/users/managers")
    UUID getManagerInfo(@RequestParam("categoryName") String categoryName);

}
