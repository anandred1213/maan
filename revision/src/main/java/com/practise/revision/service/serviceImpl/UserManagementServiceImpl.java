package com.practise.revision.service.serviceImpl;

import com.practise.revision.dto.UserDto;
import com.practise.revision.dto.UserEvent;
import com.practise.revision.entity.User;
import com.practise.revision.exceptionHandling.exceptions.UserAlreadyExistException;
import com.practise.revision.exceptionHandling.exceptions.UserNotFoundException;
import com.practise.revision.producer.UserEventProducer;
import com.practise.revision.repository.UserRepository;
import com.practise.revision.service.UserManagementService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

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

        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

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
        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new UserNotFoundException("user not found with the id: " + userDto.getId()));
        
        user.setName(userDto.getName());

        User save = userRepository.save(user);
        UserDto userDto1 = new UserDto();
        BeanUtils.copyProperties(save, userDto1);
        return userDto1;
    }

    public void updateUserFallback(UserDto userDto, Throwable throwable) {
        log.warn("Inside fallback method of update user for userId: {}", userDto.getId());
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
