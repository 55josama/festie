package com.ojosama.userservice.application.service;

import com.ojosama.userservice.application.dto.command.CreateUserCommand;
import com.ojosama.userservice.application.dto.result.CreateUserResult;
import com.ojosama.userservice.domain.model.User;
import com.ojosama.userservice.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    //유저 생성
    @Transactional
    public CreateUserResult createUser(CreateUserCommand command) {
        User user = User.create(
                command.email(),
                command.password(),
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
}
