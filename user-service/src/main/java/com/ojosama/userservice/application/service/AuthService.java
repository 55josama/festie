package com.ojosama.userservice.application.service;

import com.ojosama.userservice.application.dto.command.LoginCommand;
import com.ojosama.userservice.application.dto.command.ReissueTokenCommand;
import com.ojosama.userservice.application.dto.result.LoginResult;
import com.ojosama.userservice.application.dto.result.LogoutResult;
import com.ojosama.userservice.domain.model.User;
import com.ojosama.userservice.domain.repository.UserRepository;
import com.ojosama.userservice.global.security.JwtTokenProvider;
import com.ojosama.userservice.global.security.RefreshTokenHasher;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenHasher refreshTokenHasher;

    //로그인
    @Transactional
    public LoginResult login(LoginCommand command) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(command.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 혹은 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 혹은 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        String refreshTokenHash = refreshTokenHasher.hash(refreshToken);

        user.updateRefreshTokenHash(refreshTokenHash);

        return new LoginResult(
                accessToken,
                refreshToken
        );
    }

    //재발급
    @Transactional
    public LoginResult reissue(ReissueTokenCommand command) {
        String refreshToken = command.refreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)
                || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        UUID userId = jwtTokenProvider.getUserId(refreshToken);

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String oldRefreshTokenHash = refreshTokenHasher.hash(refreshToken);

        String newAccessToken = jwtTokenProvider.createAccessToken(user);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user);
        String newRefreshTokenHash = refreshTokenHasher.hash(newRefreshToken);

        int updatedCount = userRepository.rotateRefreshTokenHash(
                userId,
                oldRefreshTokenHash,
                newRefreshTokenHash
        );

        if (updatedCount != 1) {
            throw new IllegalArgumentException("Refresh Token이 일치하지 않습니다.");
        }

        return new LoginResult(
                newAccessToken,
                newRefreshToken
        );
    }

    //로그아웃
    @Transactional
    public LogoutResult logout(UUID userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.clearRefreshTokenHash();

        return new LogoutResult(user.getId());
    }
}