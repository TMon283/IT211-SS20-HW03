package org.example.bai3.controller;

import lombok.RequiredArgsConstructor;
import org.example.bai3.dto.AuthResponse;
import org.example.bai3.dto.LoginRequest;
import org.example.bai3.dto.RefreshTokenRequest;
import org.example.bai3.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** POST /api/inventory/auth/login */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /** POST /api/inventory/auth/refresh */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    /** POST /api/inventory/auth/logout  (requires valid access token) */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.noContent().build();
    }
}
