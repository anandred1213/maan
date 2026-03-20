package com.practise.revision.service;

import com.practise.revision.dto.UserDto;
import com.practise.revision.dto.UserEvent;
import com.practise.revision.entity.User;
import com.practise.revision.exceptionHandling.exceptions.UserNotFoundException;
import com.practise.revision.producer.UserEventProducer;
import com.practise.revision.repository.UserRepository;
import com.practise.revision.security.JwtUtil;
import com.practise.revision.service.serviceImpl.UserManagementServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEventProducer userEventProducer;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserManagementServiceImpl userService;

    @Test
    void createUser_Success() {
        UserDto userDto = new UserDto();
        userDto.setName("John Doe");
        userDto.setEmail("john@example.com");
        userDto.setPassword("password123");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("John Doe");
        savedUser.setEmail("john@example.com");

        when(userRepository.findByEmail("john@example.com")).thenReturn(null);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        doNothing().when(userEventProducer).sendUserCreatedEvent(any(UserEvent.class));

        UserDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getUser_Success() {
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getUser(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUser(1L));
        verify(userRepository).findById(1L);
    }

    @Test
    void updateUser_Success() {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Updated Name");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Old Name");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("Updated Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDto result = userService.updateUser(userDto);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository).deleteById(1L);
    }
}
