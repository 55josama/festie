package com.ojosama.userservice.global.security;

import com.ojosama.userservice.domain.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String createAccessToken(User user) {
        return createToken(user, jwtProperties.accessTokenExpiration());
    }

    public String createRefreshToken(User user) {
        return createToken(user, jwtProperties.refreshTokenExpiration());
    }

    private String createToken(User user, long expiration) {
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiredAt)
                .signWith(secretKey)
                .compact();
    }
}