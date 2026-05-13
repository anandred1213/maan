package com.practise.userservice.service.serviceImpl;

import com.practise.userservice.dto.UserDto;
import com.practise.userservice.dto.UserEvent;
import com.practise.userservice.dto.UserResponse;
import com.practise.userservice.entity.User;
import com.practise.userservice.exceptionHandling.exceptions.UserAlreadyExistException;
import com.practise.userservice.exceptionHandling.exceptions.UserNotFoundException;
import com.practise.userservice.producer.UserEventProducer;
import com.practise.userservice.repository.UserRepository;
import com.practise.userservice.service.UserManagementService;
import com.practise.userservice.utils.Role;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.practise.userservice.utils.UserResponseMessage.buildUserResponse;

@Service
public class UserManagementServiceImpl implements UserManagementService {

    private static final Logger log = LoggerFactory.getLogger(UserManagementServiceImpl.class);
    private final UserRepository userRepository;
    private final UserEventProducer userEventProducer;
    private final PasswordEncoder passwordEncoder;

    public UserManagementServiceImpl(UserRepository userRepository,
                                     UserEventProducer userEventProducer,
                                     PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userEventProducer = userEventProducer;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse createUser(UserDto userDto) {
        if (userDto.getPassword() == null) {
            throw new IllegalArgumentException("Password is required");
        }
        if (userDto.getConfirmPassword() == null) {
            throw new IllegalArgumentException("Confirm password is required");
        }
        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password do not match");
        }

        User byEmail = userRepository.findByEmail(userDto.getEmail());
        if (byEmail != null) {
            throw new UserAlreadyExistException("User already exists with this email");
        }

        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(Set.of(Role.USER));

        User saveToDb = userRepository.save(user);
        userEventProducer.sendUserCreatedEvent(new UserEvent(saveToDb.getId(), saveToDb.getName(), saveToDb.getEmail()));

        return buildUserResponse(saveToDb, "User registered successfully");
    }

    @Override
    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return buildUserResponse(user, null);
    }

    @Override
    @CircuitBreaker(name = "updateUserCB", fallbackMethod = "updateUserFallback")
    public UserResponse updateUser(UserDto userDto) {
        if (userDto.getId() == null) {
            throw new IllegalArgumentException("User id must not be null");
        }
        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userDto.getId()));

        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());

        User save = userRepository.save(user);
        return buildUserResponse(save, "User updated successfully");
    }

    public UserResponse updateUserFallback(UserDto userDto, Throwable throwable) {
        log.warn("Circuit breaker triggered for updateUser, userId: {}, reason: {}",
                userDto.getId(), throwable.getMessage());
        UserResponse fallback = new UserResponse();
        fallback.setId(userDto.getId());
        fallback.setName(userDto.getName());
        fallback.setEmail(userDto.getEmail());
        fallback.setMessage("Update temporarily unavailable, please try again later");
        return fallback;
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> buildUserResponse(user, null))
                .toList();
    }

}
