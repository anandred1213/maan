package com.practise.userservice.service.serviceImpl;

import com.practise.userservice.dto.TokenRefreshRequest;
import com.practise.userservice.dto.TokenRefreshResponse;
import com.practise.userservice.entity.RefreshToken;
import com.practise.userservice.entity.User;
import com.practise.userservice.repository.UserRepository;
import com.practise.userservice.security.CustomUserDetailsService;
import com.practise.userservice.security.JwtUtil;
import com.practise.userservice.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserAuthenticationServiceImpl userAuthenticationService;

    @Test
    void validateUser_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);

        boolean result = userAuthenticationService.validateUser("test@example.com", "password");

        assertTrue(result);
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password", "encoded-password");
    }

    @Test
    void validateUser_InvalidPassword() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        boolean result = userAuthenticationService.validateUser("test@example.com", "wrong-password");

        assertFalse(result);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void validateUser_UserNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(null);

        boolean result = userAuthenticationService.validateUser("test@example.com", "password");

        assertFalse(result);
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void refreshToken_Success() {
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("refresh-token");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserEmail("test@example.com");
        refreshToken.setToken("refresh-token");
        refreshToken.setExpiryDate(Instant.now().plusMillis(100000));

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken("new-refresh-token");

        UserDetails userDetails = mock(UserDetails.class);

        when(refreshTokenService.findByToken("refresh-token")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);
        when(customUserDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("new-access-token");
        doNothing().when(refreshTokenService).deleteByUserEmail("test@example.com");
        when(refreshTokenService.createRefreshToken("test@example.com")).thenReturn(newRefreshToken);

        TokenRefreshResponse result = userAuthenticationService.refreshToken(request);

        assertNotNull(result);
        assertEquals("new-access-token", result.getAccessToken());
        assertEquals("new-refresh-token", result.getRefreshToken());
        verify(refreshTokenService).findByToken("refresh-token");
        verify(refreshTokenService).verifyExpiration(refreshToken);
        verify(refreshTokenService).deleteByUserEmail("test@example.com");
        verify(refreshTokenService).createRefreshToken("test@example.com");
    }

    @Test
    void refreshToken_InvalidToken() {
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("invalid-token");

        when(refreshTokenService.findByToken("invalid-token")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userAuthenticationService.refreshToken(request));
        verify(refreshTokenService).findByToken("invalid-token");
    }
}
