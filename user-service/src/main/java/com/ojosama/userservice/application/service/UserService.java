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
import com.ojosama.userservice.domain.model.Category;
import com.ojosama.userservice.domain.model.User;
import com.ojosama.userservice.domain.model.UserRole;
import com.ojosama.userservice.domain.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //회원가입
    @Transactional
    public CreateUserResult createUser(CreateUserCommand command) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(command.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (userRepository.existsByNicknameAndDeletedAtIsNull(command.nickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
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
                throw new IllegalArgumentException("중복 이메일입니다.", e);
            }

            if (isNicknameUniqueViolation(e)) {
                throw new IllegalArgumentException("중복 닉네임입니다.", e);
            }

            throw e;
        }
    }

    //유저 조회 todo: 관리자 전용, 로그인 사용자 기준 본인만 조회 기능 추가
    @Transactional(readOnly = true)
    public GetUserResult getUser(GetUserQuery query) {
        User user = userRepository.findByIdAndDeletedAtIsNull(query.userId())
                //todo 임시 오류 처리
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));

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
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        if (userRepository.existsByNicknameAndIdNotAndDeletedAtIsNull(command.nickname(), command.userId())) {
            throw new IllegalArgumentException("중복 닉네임입니다.");
        }

        savedUser.update(
                command.name(),
                command.nickname(),
                command.phoneNumber()
        );

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
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

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

    @Transactional(readOnly = true)
    public InternalUserEmailResult getInternalUserEmail(GetInternalUserEmailQuery query) {
        User user = userRepository.findByIdAndDeletedAtIsNull(query.userId())
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        return new InternalUserEmailResult(
                user.getId(),
                user.getEmail()
        );
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
                .orElseThrow(() -> new IllegalArgumentException("시스템 관리자를 찾을 수 없습니다."));

        return new GetAdminIdResult(admin.getId());
    }

    @Transactional(readOnly = true)
    public GetCategoryManagerIdResult getInternalManagerId(GetCategoryManagerQuery query) {
        UserRole managerRole = Category.from(query.category()).getManagerRole();

        User manager = userRepository.findFirstByRoleAndDeletedAtIsNull(managerRole)
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리 담당자를 찾을 수 없습니다."));

        return new GetCategoryManagerIdResult(manager.getId());
    }

    //userId로 nickname 조회
    @Transactional(readOnly = true)
    public String getInternalUserNickname(UUID userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        return user.getNickname();
    }
}


