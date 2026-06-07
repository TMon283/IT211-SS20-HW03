package org.example.bai3.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bai3.entity.AppUser;
import org.example.bai3.repository.AppUserRepository;
import org.example.bai3.repository.UserTokenRepository;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserTokenRepository userTokenRepository;
    private final AppUserRepository appUserRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            final String username = jwtUtils.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // ── Mandatory DB check: reject access tokens whose session is revoked ──
                // We store only Refresh Tokens in DB. An active session means the user
                // has at least one non-revoked, non-expired refresh token in the DB.
                // On logout all tokens are revoked, so subsequent access-token calls fail.
                AppUser appUser = appUserRepository.findByUsername(username)
                        .orElse(null);

                boolean hasActiveSession = appUser != null &&
                        !userTokenRepository
                                .findByUserIdAndRevokedFalseAndExpiredFalse(appUser.getId())
                                .isEmpty();

                if (jwtUtils.isTokenValid(jwt, userDetails) && hasActiveSession) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ex) {
            log.warn("JWT validation failed: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
