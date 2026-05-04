package com.ojosama.notificationservice.infrastructure.client.fallback;

import com.ojosama.common.exception.CommonErrorCode;
import com.ojosama.common.exception.CustomException;
import com.ojosama.notificationservice.domain.exception.NotificationErrorCode;
import com.ojosama.notificationservice.domain.exception.NotificationException;
import com.ojosama.notificationservice.infrastructure.client.UserClient;
import com.ojosama.notificationservice.infrastructure.client.dto.UserInfo;
import feign.FeignException;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserClientFallBackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {
            @Override
            public UserInfo getUserInfo(List<UUID> userIds) {
                log.error("UserClientFallBackFactory: {}", cause.getMessage());
                if (cause instanceof FeignException.NotFound) {
                    throw new NotificationException(NotificationErrorCode.NOT_FOUND_USER);
                }
                throw new CustomException(CommonErrorCode.UNEXPECTED_ERROR);
            }

            @Override
            public String getUserEmail(UUID userId) {
                log.error("UserClientFallBackFactory: {}", cause.getMessage());
                if (cause instanceof FeignException.NotFound) {
                    throw new NotificationException(NotificationErrorCode.NOT_FOUND_USER);
                }
                throw new CustomException(CommonErrorCode.UNEXPECTED_ERROR);
            }

            @Override
            public UUID getAdminInfo() {
                log.error("UserClientFallBackFactory: {}", cause.getMessage());
                if (cause instanceof FeignException.NotFound) {
                    throw new NotificationException(NotificationErrorCode.NOT_FOUND_USER);
                }
                throw new CustomException(CommonErrorCode.UNEXPECTED_ERROR);
            }

            @Override
            public UUID getManagerInfo(String categoryName) {
                log.error("UserClientFallBackFactory: {}", cause.getMessage());
                if (cause instanceof FeignException.NotFound) {
                    throw new NotificationException(NotificationErrorCode.NOT_FOUND_USER);
                }
                throw new CustomException(CommonErrorCode.UNEXPECTED_ERROR);
            }
        };
    }
}
