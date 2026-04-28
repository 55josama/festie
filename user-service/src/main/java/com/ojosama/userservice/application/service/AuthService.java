package com.ojosama.userservice.application.service;

import com.ojosama.userservice.application.dto.command.LoginCommand;
import com.ojosama.userservice.application.dto.command.ReissueTokenCommand;
import com.ojosama.userservice.application.dto.result.LoginResult;
import com.ojosama.userservice.application.dto.result.LogoutResult;
import com.ojosama.userservice.domain.model.User;
import com.ojosama.userservice.domain.repository.UserRepository;
import com.ojosama.userservice.global.security.JwtTokenProvider;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResult login(LoginCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일이 올바르지 않습니다."));

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        user.updateRefreshToken(refreshToken);
        userRepository.save(user);

        return new LoginResult(
                accessToken,
                refreshToken
        );
    }


    //로그인
    public LoginResult reissue(ReissueTokenCommand command) {
        String refreshToken = command.refreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        UUID userId = jwtTokenProvider.getUserId(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
            throw new IllegalArgumentException("Refresh Token이 일치하지 않습니다.");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(user);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user);

        user.updateRefreshToken(newRefreshToken);
        userRepository.save(user);

        return new LoginResult(
                newAccessToken,
                newRefreshToken
        );
    }

    //로그아웃
    public LogoutResult logout(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.clearRefreshToken();
        userRepository.save(user);

        return new LogoutResult(user.getId());
    }
}