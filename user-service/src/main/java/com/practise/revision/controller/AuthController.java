package com.practise.userservice.controller;

import com.practise.userservice.dto.AuthRequest;
import com.practise.userservice.dto.AuthResponse;
import com.practise.userservice.dto.TokenRefreshRequest;
import com.practise.userservice.dto.TokenRefreshResponse;
import com.practise.userservice.entity.RefreshToken;
import com.practise.userservice.security.JwtUtil;
import com.practise.userservice.service.RefreshTokenService;
import com.practise.userservice.service.UserAuthenticationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * 
 * Handles user authentication, JWT token generation, and token refresh.
 * These endpoints are public (configured in SecurityConfig).
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    
    private final JwtUtil jwtUtil;
    private final UserAuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(JwtUtil jwtUtil,
                          UserAuthenticationService authenticationService,
                          RefreshTokenService refreshTokenService) {
        this.jwtUtil = jwtUtil;
        this.authenticationService = authenticationService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Login Endpoint
     * 
     * Flow:
     * 1. Receive email and password from request body
     * 2. Validate credentials against database
     * 3. If valid, generate JWT access token (15 min) and refresh token (7 days)
     * 4. Return both tokens in response
     * 5. If invalid, return 401 Unauthorized
     * 
     * @param request Contains email and password
     * @return Access token and refresh token if credentials valid, 401 otherwise
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        log.info("Login attempt for user: {}", request.getEmail());
        AuthResponse response = authenticationService.login(request);
        log.info("Login successful for user: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh Token Endpoint
     * 
     * Flow:
     * 1. Receive refresh token from request
     * 2. Validate refresh token exists and not expired
     * 3. Generate new access token
     * 4. Return new access token with same refresh token
     * 5. If invalid/expired, return 403 Forbidden
     * 
     * @param request Contains refresh token
     * @return New access token and refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }

    /**
     * Logout Endpoint
     * 
     * Deletes refresh token from database
     * 
     * @param request Contains refresh token to invalidate
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        log.info("Logout request received");
        
        refreshTokenService.findByToken(refreshToken).ifPresent(token -> {
            refreshTokenService.deleteByUserEmail(token.getUserEmail());
            log.info("User logged out successfully: {}", token.getUserEmail());
        });
        
        return ResponseEntity.ok("Logged out successfully");
    }
}
