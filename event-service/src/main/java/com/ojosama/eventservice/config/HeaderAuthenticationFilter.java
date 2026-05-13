package com.ojosama.eventservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String userId = request.getHeader(USER_ID_HEADER);
        String role = request.getHeader(USER_ROLE_HEADER);

        log.debug("HeaderAuthenticationFilter - userId: {}, role: {}", userId, role);

        if (userId != null && !userId.isBlank() && role != null && !role.isBlank()) {
            String trimmedUserId = userId.trim();
            String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
            String authority = normalizedRole.startsWith("ROLE_")
                    ? normalizedRole
                    : "ROLE_" + normalizedRole;

            if (isValidUUID(trimmedUserId)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                trimmedUserId,
                                null,
                                List.of(new SimpleGrantedAuthority(authority))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.warn("Invalid UUID format for X-User-Id header: {}", trimmedUserId);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isValidUUID(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
