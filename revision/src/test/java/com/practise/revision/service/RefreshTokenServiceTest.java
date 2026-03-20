package com.practise.revision.service;

import com.practise.revision.entity.RefreshToken;
import com.practise.revision.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void createRefreshToken_Success() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDuration", 604800000L);

        RefreshToken savedToken = new RefreshToken();
        savedToken.setId(1L);
        savedToken.setUserEmail("test@example.com");
        savedToken.setToken("test-token");
        savedToken.setExpiryDate(Instant.now().plusMillis(604800000L));

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(savedToken);

        RefreshToken result = refreshTokenService.createRefreshToken("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getUserEmail());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void findByToken_Success() {
        RefreshToken token = new RefreshToken();
        token.setToken("test-token");

        when(refreshTokenRepository.findByToken("test-token")).thenReturn(Optional.of(token));

        Optional<RefreshToken> result = refreshTokenService.findByToken("test-token");

        assertTrue(result.isPresent());
        assertEquals("test-token", result.get().getToken());
        verify(refreshTokenRepository).findByToken("test-token");
    }

    @Test
    void verifyExpiration_ValidToken() {
        RefreshToken token = new RefreshToken();
        token.setUserEmail("test@example.com");
        token.setExpiryDate(Instant.now().plusMillis(100000));

        RefreshToken result = refreshTokenService.verifyExpiration(token);

        assertNotNull(result);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void verifyExpiration_ExpiredToken() {
        RefreshToken token = new RefreshToken();
        token.setUserEmail("test@example.com");
        token.setExpiryDate(Instant.now().minusMillis(100000));

        doNothing().when(refreshTokenRepository).delete(token);

        assertThrows(RuntimeException.class, () -> refreshTokenService.verifyExpiration(token));
        verify(refreshTokenRepository).delete(token);
    }

    @Test
    void deleteByUserEmail_Success() {
        doNothing().when(refreshTokenRepository).deleteByUserEmail("test@example.com");

        assertDoesNotThrow(() -> refreshTokenService.deleteByUserEmail("test@example.com"));
        verify(refreshTokenRepository).deleteByUserEmail("test@example.com");
    }
}
