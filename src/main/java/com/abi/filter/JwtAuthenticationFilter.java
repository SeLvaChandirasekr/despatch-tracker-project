package com.abi.filter;

import com.abi.constant.SecurityConstants;
import com.abi.security.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull final HttpServletRequest request,
                                     @NonNull final HttpServletResponse response,
                                     @NonNull final FilterChain filterChain) throws ServletException, IOException {
        final String token = resolveToken(request);
        log.warn("JWT-DEBUG [{} {}] token present: {}", request.getMethod(), request.getRequestURI(), token != null);

        if (token != null) {
            final boolean valid = jwtTokenProvider.isTokenValid(token);
            log.warn("JWT-DEBUG [{} {}] token valid: {}", request.getMethod(), request.getRequestURI(), valid);

            if (valid && SecurityContextHolder.getContext().getAuthentication() == null) {
                final String username = jwtTokenProvider.extractUsername(token);
                final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                final UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.warn("JWT-DEBUG [{} {}] authenticated as: {}", request.getMethod(), request.getRequestURI(), username);
            }
        } else {
            log.warn("JWT-DEBUG [{} {}] no Bearer header or token param - request will be anonymous", request.getMethod(), request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }

    // Browsers' native EventSource can't set an Authorization header, so GET /dashboard/events
    // needs a query-param fallback. Header still wins when both are present.
    private String resolveToken(final HttpServletRequest request) {
        final String authHeader = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            return authHeader.substring(SecurityConstants.BEARER_PREFIX.length());
        }
        return request.getParameter("token");
    }
}
