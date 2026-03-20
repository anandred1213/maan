package com.practise.revision.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practise.revision.dto.*;
import com.practise.revision.entity.RefreshToken;
import com.practise.revision.security.JwtUtil;
import com.practise.revision.service.RefreshTokenService;
import com.practise.revision.service.UserAuthenticationService;
import com.practise.revision.service.UserManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserAuthenticationService authenticationService;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private UserManagementService userManagementService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_Success() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        AuthResponse response = new AuthResponse("access-token", "refresh-token");

        when(userManagementService.login(any(AuthRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(userManagementService).login(any(AuthRequest.class));
    }

    @Test
    void refreshToken_Success() throws Exception {
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("refresh-token");

        TokenRefreshResponse response = new TokenRefreshResponse("new-access-token", "new-refresh-token");

        when(authenticationService.refreshToken(any(TokenRefreshRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));

        verify(authenticationService).refreshToken(any(TokenRefreshRequest.class));
    }

    @Test
    void logout_Success() throws Exception {
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("refresh-token");

        RefreshToken token = new RefreshToken();
        token.setUserEmail("test@example.com");
        token.setToken("refresh-token");
        token.setExpiryDate(Instant.now().plusMillis(100000));

        when(refreshTokenService.findByToken("refresh-token")).thenReturn(Optional.of(token));
        doNothing().when(refreshTokenService).deleteByUserEmail("test@example.com");

        mockMvc.perform(post("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully"));

        verify(refreshTokenService).findByToken("refresh-token");
        verify(refreshTokenService).deleteByUserEmail("test@example.com");
    }
}
