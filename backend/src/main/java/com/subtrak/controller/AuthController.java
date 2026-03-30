package com.subtrak.controller;

import com.subtrak.dto.request.LoginRequest;
import com.subtrak.dto.request.RefreshTokenRequest;
import com.subtrak.dto.request.RegisterRequest;
import com.subtrak.dto.response.AuthResponse;
import com.subtrak.exception.RateLimitExceededException;
import com.subtrak.security.RateLimiter;
import com.subtrak.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final RateLimiter rateLimiter;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        checkAuthRateLimit(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        checkAuthRateLimit(httpRequest);
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (Exception e) {
            log.warn("Failed login attempt for email: {} from IP: {}", request.email, getClientIp(httpRequest));
            throw e;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest
    ) {
        checkAuthRateLimit(httpRequest);
        return ResponseEntity.ok(authService.refresh(request.refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        authService.logout(request.refreshToken);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    private void checkAuthRateLimit(HttpServletRequest request) {
        String ip = getClientIp(request);
        if (!rateLimiter.tryConsumeAuth(ip)) {
            log.warn("Rate limit exceeded for IP: {}", ip);
            throw new RateLimitExceededException();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
