package org.example.bai3.service;

import lombok.RequiredArgsConstructor;
import org.example.bai3.dto.AuthResponse;
import org.example.bai3.dto.LoginRequest;
import org.example.bai3.dto.RefreshTokenRequest;
import org.example.bai3.entity.AppUser;
import org.example.bai3.entity.UserToken;
import org.example.bai3.repository.AppUserRepository;
import org.example.bai3.repository.UserTokenRepository;
import org.example.bai3.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final UserTokenRepository userTokenRepository;
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    // ── Login ────────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Throws AuthenticationException on bad credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        AppUser user = appUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken  = jwtUtils.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());

        // Persist refresh token
        UserToken tokenEntity = UserToken.builder()
                .refreshToken(refreshToken)
                .revoked(false)
                .expired(false)
                .user(user)
                .build();
        userTokenRepository.save(tokenEntity);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // ── Refresh ──────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String incomingRefreshToken = request.getRefreshToken();

        UserToken tokenEntity = userTokenRepository
                .findByRefreshToken(incomingRefreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (tokenEntity.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }
        if (tokenEntity.isExpired() || jwtUtils.isTokenExpired(incomingRefreshToken)) {
            tokenEntity.setExpired(true);
            userTokenRepository.save(tokenEntity);
            throw new RuntimeException("Refresh token has expired");
        }

        String username = jwtUtils.extractUsername(incomingRefreshToken);
        String newAccessToken = jwtUtils.generateAccessToken(username);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(incomingRefreshToken) // same refresh token, still valid
                .build();
    }

    // ── Logout ───────────────────────────────────────────────────────────────

    @Transactional
    public void logout() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Mark ALL active tokens as revoked — Stream API replaces the for-loop
        userTokenRepository
                .findByUserIdAndRevokedFalseAndExpiredFalse(user.getId())
                .stream()
                .peek(token -> token.setRevoked(true))
                .forEach(userTokenRepository::save);
    }
}
