package com.practise.userservice.service;

import com.practise.userservice.dto.AuthRequest;
import com.practise.userservice.dto.AuthResponse;
import com.practise.userservice.dto.TokenRefreshRequest;
import com.practise.userservice.dto.TokenRefreshResponse;

public interface UserAuthenticationService {
    boolean validateUser(String email, String password);
    AuthResponse login(AuthRequest request);
    TokenRefreshResponse refreshToken(TokenRefreshRequest request);
}
