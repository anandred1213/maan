package com.practise.userservice.service.serviceImpl;

import com.practise.userservice.dto.UserDto;
import com.practise.userservice.dto.UserEvent;
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
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

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
    public UserDto createUser(UserDto userDto) {
        User byEmail = userRepository.findByEmail(userDto.getEmail());
        if(byEmail != null) {
            throw new UserAlreadyExistException("user already found with the email");
        }

        if(!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            throw  new IllegalArgumentException("password and confirm password do not match");
        }

        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(Set.of(Role.USER));

        User saveToDb = userRepository.save(user);
        UserDto userDto1 = new UserDto();
        BeanUtils.copyProperties(saveToDb, userDto1);

        userEventProducer.sendUserCreatedEvent(new UserEvent(saveToDb.getId(), saveToDb.getName(), saveToDb.getEmail()));
        return userDto1;
    }

    @Override
    public UserDto getUser(Long id) {
        return userRepository.findById(id)
                .map(this::convertToUserDto)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public UserDto readUserFallback(Long id, Throwable throwable) {
        UserDto fallbackUser = new UserDto();
        fallbackUser.setId(id);
        fallbackUser.setName("user");
        fallbackUser.setEmail("user@gmail.com");
        return fallbackUser;
    }

    @Override
    @CircuitBreaker(name="updateUserCB", fallbackMethod = "updateUserFallback")
    public UserDto updateUser(UserDto userDto) {
        if (userDto.getId() == null) {
            throw new IllegalArgumentException("User id must not be null");
        }
        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new UserNotFoundException("user not found with the id: " + userDto.getId()));

        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());

        User save = userRepository.save(user);
        UserDto userDto1 = new UserDto();
        BeanUtils.copyProperties(save, userDto1);
        return userDto1;
    }

    public UserDto updateUserFallback(UserDto userDto, Throwable throwable) {
        log.warn("Circuit breaker triggered for updateUser, userId: {}, reason: {}",
                userDto.getId(), throwable.getMessage());
        UserDto fallback = new UserDto();
        fallback.setId(userDto.getId());
        fallback.setName(userDto.getName());
        fallback.setEmail(userDto.getEmail());
        return fallback;
    }


    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<UserDto> getAllUsers(){
        return userRepository.findAll()
         .stream()
         .map(this::convertToUserDto)
         .toList();
    }

    private UserDto convertToUserDto(User user) {
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        return userDto;
    }
}
