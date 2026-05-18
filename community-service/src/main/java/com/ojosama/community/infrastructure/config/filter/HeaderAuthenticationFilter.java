package com.ojosama.community.infrastructure.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Gateway 가 내려주는 X-User-Id / X-User-Role 헤더를 읽어 SecurityContext 에 등록.
 * JWT 검증은 Gateway 에서 완료됐으므로 여기서는 헤더 신뢰 후 인증 객체만 생성한다.
 */
@Slf4j
@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String userId = request.getHeader(USER_ID_HEADER);
        String role   = request.getHeader(USER_ROLE_HEADER);

        if (userId != null && !userId.isBlank() && role != null && !role.isBlank()) {
            try {
                UUID userIdUuid = UUID.fromString(userId.trim());

                // "ROLE_" 접두사 중복 방지
                String normalizedRole = role.trim().toUpperCase();
                if (normalizedRole.startsWith("ROLE_")) {
                    normalizedRole = normalizedRole.substring(5);
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userIdUuid,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + normalizedRole))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (IllegalArgumentException e) {
                log.warn("[HeaderAuthFilter] 잘못된 X-User-Id 형식: {}", userId);
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authentication headers");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
