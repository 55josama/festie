package com.ojosama.userservice.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class InternalRequestFilter extends OncePerRequestFilter {

    private static final String INTERNAL_PATH_PATTERN = "/internal/**";
    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private final InternalApiProperties internalApiProperties;
    private final Environment environment;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String servletPath = request.getServletPath();

        if (!pathMatcher.match(INTERNAL_PATH_PATTERN, servletPath)
                || environment.acceptsProfiles(Profiles.of("local", "test"))) {
            filterChain.doFilter(request, response);
            return;
        }

        String configuredToken = internalApiProperties.token();
        String requestToken = request.getHeader(INTERNAL_TOKEN_HEADER);

        if (!StringUtils.hasText(configuredToken) || !configuredToken.equals(requestToken)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        filterChain.doFilter(request, response);
    }
}
