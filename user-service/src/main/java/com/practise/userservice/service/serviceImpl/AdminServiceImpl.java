package com.practise.userservice.service.serviceImpl;

import com.practise.userservice.dto.UserDto;
import com.practise.userservice.dto.UserEvent;
import com.practise.userservice.dto.UserResponse;
import com.practise.userservice.entity.User;
import com.practise.userservice.exceptionHandling.exceptions.UserAlreadyExistException;
import com.practise.userservice.producer.UserEventProducer;
import com.practise.userservice.repository.UserRepository;
import com.practise.userservice.service.IAdminService;
import com.practise.userservice.utils.Role;
import com.practise.userservice.utils.UserResponseMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AdminServiceImpl  implements IAdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEventProducer userEventProducer;

    public AdminServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, UserEventProducer userEventProducer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userEventProducer = userEventProducer;
    }

    @Override
    public UserResponse createAdmin(UserDto userDto) {
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
        user.setRole(Set.of(Role.ADMIN));

        User saveToDb = userRepository.save(user);
        userEventProducer.sendUserCreatedEvent(new UserEvent(saveToDb.getId(), saveToDb.getName(), saveToDb.getEmail()));

        return UserResponseMessage.buildUserResponse(saveToDb, "User registered successfully");
    }


}
