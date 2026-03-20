package com.practise.revision.service.serviceImpl;

import com.practise.revision.dto.OrderDto;
import com.practise.revision.dto.UserDto;
import com.practise.revision.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Facade service that delegates to specialized services
 * Follows SOLID principles by composing focused services
 */
@Service
public class UserServiceImpl implements UserService {
    
    private final UserManagementService userManagementService;
    private final UserAuthenticationService authenticationService;
    private final OrderService orderService;


    public UserServiceImpl(UserManagementService userManagementService,
                          UserAuthenticationService authenticationService,
                          OrderService orderService) {
        this.userManagementService = userManagementService;
        this.authenticationService = authenticationService;
        this.orderService = orderService;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        return userManagementService.createUser(userDto);
    }

    @Override
    public UserDto getUser(Long id) {
        return userManagementService.getUser(id);
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        return userManagementService.updateUser(userDto);
    }

    @Override
    public void deleteUser(Long id) {
        userManagementService.deleteUser(id);
    }

    @Override
    public List<OrderDto> getUserOrders(Long userId) {
        return orderService.getUserOrders(userId);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userManagementService.getAllUsers();
    }

    @Override
    public boolean validateUser(String email, String password) {
        return authenticationService.validateUser(email, password);
    }
}
