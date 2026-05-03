package com.ojosama.userservice.presentation.controller;

import com.ojosama.userservice.domain.model.User;
import com.ojosama.userservice.domain.model.UserRole;
import com.ojosama.userservice.domain.repository.UserRepository;
import com.ojosama.userservice.presentation.dto.request.CreateUserRequestDto;
import com.ojosama.userservice.presentation.dto.response.CreateUserResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Profile({"local", "test"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/dev/users")
public class DevUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/admin")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponseDto createAdminUser(
            @Valid @RequestBody CreateUserRequestDto request
    ) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("중복 이메일입니다.");
        }

        User adminUser = User.create(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name(),
                request.nickname(),
                request.phoneNumber()
        );

        adminUser.changeRole(UserRole.ADMIN);

        User savedUser = userRepository.save(adminUser);

        return new CreateUserResponseDto(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getNickname(),
                savedUser.getName(),
                savedUser.getRole()
        );
    }
}