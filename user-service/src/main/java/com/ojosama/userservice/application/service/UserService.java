package com.ojosama.userservice.application.service;

import com.ojosama.userservice.application.dto.command.CreateUserCommand;
import com.ojosama.userservice.application.dto.command.DeleteUserCommand;
import com.ojosama.userservice.application.dto.command.UpdateUserCommand;
import com.ojosama.userservice.application.dto.query.GetUserQuery;
import com.ojosama.userservice.application.dto.result.CreateUserResult;
import com.ojosama.userservice.application.dto.result.GetUserResult;
import com.ojosama.userservice.application.dto.result.UpdateUserResult;
import com.ojosama.userservice.domain.model.User;
import com.ojosama.userservice.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //유저 생성
    @Transactional
    public CreateUserResult createUser(CreateUserCommand command) {

        String encodedPassword = passwordEncoder.encode(command.password());

        User user = User.create(
                command.email(),
                encodedPassword,
                command.nickname(),
                command.name(),
                command.phoneNumber()
        );

        User savedUser = userRepository.save(user);

        return new CreateUserResult(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getNickname(),
                savedUser.getName(),
                savedUser.getRole()
        );
    }

    //유저 조회 todo: 관리자 전용, 로그인 사용자 기준 본인만 조회 기능 추가
    @Transactional(readOnly = true)
    public GetUserResult getUser(GetUserQuery query) {
        User user = userRepository.findById(query.userId())
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

    //유저 수정
    @Transactional
    public UpdateUserResult updateUser(UpdateUserCommand command) {
        User savedUser = userRepository.findById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        savedUser.update(command.email(), command.nickname());

        User updatedUser = userRepository.save(savedUser);

        return new UpdateUserResult(
                updatedUser.getId(),
                updatedUser.getEmail(),
                updatedUser.getNickname(),
                updatedUser.getUpdatedAt()
        );
    }

    //유저 삭제
    @Transactional
    public void deleteUser(DeleteUserCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        user.deleted(user.getId());
    }
}
