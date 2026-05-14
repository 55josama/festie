package com.ojosama.userservice.application.service;

import com.ojosama.userservice.application.dto.command.CreateUserCommand;
import com.ojosama.userservice.application.dto.command.DeleteUserCommand;
import com.ojosama.userservice.application.dto.command.UpdateUserCommand;
import com.ojosama.userservice.application.dto.query.GetCategoryManagerQuery;
import com.ojosama.userservice.application.dto.query.GetInternalUserEmailQuery;
import com.ojosama.userservice.application.dto.query.GetUserQuery;
import com.ojosama.userservice.application.dto.result.CreateUserResult;
import com.ojosama.userservice.application.dto.result.GetAdminIdResult;
import com.ojosama.userservice.application.dto.result.GetCategoryManagerIdResult;
import com.ojosama.userservice.application.dto.result.GetUserResult;
import com.ojosama.userservice.application.dto.result.InternalUserEmailResult;
import com.ojosama.userservice.application.dto.result.UpdateUserResult;
import com.ojosama.userservice.domain.exception.UserErrorCode;
import com.ojosama.userservice.domain.exception.UserException;
import com.ojosama.userservice.domain.model.Category;
import com.ojosama.userservice.domain.model.User;
import com.ojosama.userservice.domain.model.UserRole;
import com.ojosama.userservice.domain.repository.UserRepository;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final String NICKNAME_CACHE_KEY_PREFIX = "chat:nickname:";
    private static final String USER_EMAIL_CACHE_KEY_PREFIX = "user:email:";
    private static final Duration USER_CACHE_TTL = Duration.ofMinutes(10);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    //유저 생성
    @Transactional
    public CreateUserResult createUser(CreateUserCommand command) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(command.email())) {
            throw new UserException(UserErrorCode.DUPLICATE_EMAIL);
        }

        if (userRepository.existsByNicknameAndDeletedAtIsNull(command.nickname())) {
            throw new UserException(UserErrorCode.DUPLICATE_NICKNAME);
        }

        String encodedPassword = passwordEncoder.encode(command.password());

        User user = User.create(
                command.email(),
                encodedPassword,
                command.name(),
                command.nickname(),
                command.phoneNumber()
        );

        try {
            User savedUser = userRepository.save(user);

            return new CreateUserResult(
                    savedUser.getId(),
                    savedUser.getEmail(),
                    savedUser.getNickname(),
                    savedUser.getName(),
                    savedUser.getRole()
            );
        } catch (DataIntegrityViolationException e) {
            if (isEmailUniqueViolation(e)) {
                throw new UserException(UserErrorCode.DUPLICATE_EMAIL);
            }

            if (isNicknameUniqueViolation(e)) {
                throw new UserException(UserErrorCode.DUPLICATE_NICKNAME);
            }

            throw e;
        }
    }

    //유저 조회
    @Transactional(readOnly = true)
    public GetUserResult getUser(GetUserQuery query) {
        User user = userRepository.findByIdAndDeletedAtIsNull(query.userId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        return new GetUserResult(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    // 유저 수정
    @Transactional
    public UpdateUserResult updateUser(UpdateUserCommand command) {
        User savedUser = userRepository.findByIdAndDeletedAtIsNull(command.userId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        if (userRepository.existsByNicknameAndIdNotAndDeletedAtIsNull(command.nickname(), command.userId())) {
            throw new UserException(UserErrorCode.DUPLICATE_NICKNAME);
        }

        savedUser.update(
                command.name(),
                command.nickname(),
                command.phoneNumber()
        );

        evictNicknameCache(savedUser.getId());

        return new UpdateUserResult(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getNickname(),
                savedUser.getPhoneNumber(),
                savedUser.getUpdatedAt()
        );
    }

    //유저 삭제
    @Transactional
    public void deleteUser(DeleteUserCommand command) {
        User user = userRepository.findByIdAndDeletedAtIsNull(command.userId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        user.deleted(user.getId());
    }

    //이메일 중복 DB 저장 실패 시
    private boolean isEmailUniqueViolation(DataIntegrityViolationException e) {
        Throwable rootCause = NestedExceptionUtils.getMostSpecificCause(e);

        if (rootCause instanceof ConstraintViolationException constraintViolationException) {
            String constraintName = constraintViolationException.getConstraintName();

            return constraintName != null && constraintName.contains("email");
        }

        String message = rootCause.getMessage();

        return message != null && message.contains("email");
    }

    //닉네임 중복 DB 저장 실패 시
    private boolean isNicknameUniqueViolation(DataIntegrityViolationException e) {
        String message = NestedExceptionUtils.getMostSpecificCause(e).getMessage();
        return message != null && message.contains("nickname");
    }

    //userId로 email 조회
    @Transactional(readOnly = true)
    public InternalUserEmailResult getInternalUserEmail(GetInternalUserEmailQuery query) {
        String cacheKey = emailCacheKey(query.userId());
        String cachedEmail = redisTemplate.opsForValue().get(cacheKey);

        if (cachedEmail != null) {
            return new InternalUserEmailResult(query.userId(), cachedEmail);
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(query.userId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        redisTemplate.opsForValue().set(cacheKey, user.getEmail(), USER_CACHE_TTL);

        return new InternalUserEmailResult(user.getId(), user.getEmail());
    }

    @Transactional(readOnly = true)
    public Map<UUID, String> getInternalUserEmails(List<UUID> userIds) {
        List<User> users = userRepository.findAllByIdInAndDeletedAtIsNull(userIds);

        return users.stream()
                .collect(Collectors.toMap(
                        User::getId,
                        User::getEmail
                ));
    }

    @Transactional(readOnly = true)
    public GetAdminIdResult getInternalAdminId() {
        User admin = userRepository.findFirstByRoleAndDeletedAtIsNull(UserRole.ADMIN)
                .orElseThrow(() -> new UserException(UserErrorCode.ADMIN_NOT_FOUND));

        return new GetAdminIdResult(admin.getId());
    }

    @Transactional(readOnly = true)
    public GetCategoryManagerIdResult getInternalManagerId(GetCategoryManagerQuery query) {
        UserRole managerRole = Category.from(query.category()).getManagerRole();

        User manager = userRepository.findFirstByRoleAndDeletedAtIsNull(managerRole)
                .orElseThrow(() -> new UserException(UserErrorCode.CATEGORY_MANAGER_NOT_FOUND));

        return new GetCategoryManagerIdResult(manager.getId());
    }

    //userId로 nickname 조회
    @Transactional(readOnly = true)
    public String getInternalUserNickname(UUID userId) {
        String cacheKey = nicknameCacheKey(userId);
        String cachedNickname = redisTemplate.opsForValue().get(cacheKey);

        if (cachedNickname != null) {
            return cachedNickname;
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        redisTemplate.opsForValue().set(cacheKey, user.getNickname(), USER_CACHE_TTL);

        return user.getNickname();
    }

    private void evictNicknameCache(UUID userId) {
        try {
            redisTemplate.delete(nicknameCacheKey(userId));
        } catch (Exception e) {
            log.warn("닉네임 캐시 삭제 실패. userId={}", userId, e);
            // 캐시 삭제 실패는 닉네임 수정 자체를 막지 않는다. -> warn 로그 남김
        }
    }

    private String nicknameCacheKey(UUID userId) {
        return NICKNAME_CACHE_KEY_PREFIX + userId;
    }

    private String emailCacheKey(UUID userId) {
        return USER_EMAIL_CACHE_KEY_PREFIX + userId;
    }
}

