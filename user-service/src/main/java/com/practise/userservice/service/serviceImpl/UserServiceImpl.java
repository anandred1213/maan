package com.practise.userservice.service.serviceImpl;

import com.practise.userservice.dto.OrderDto;
import com.practise.userservice.dto.UserDto;
import com.practise.userservice.dto.UserResponse;
import com.practise.userservice.service.*;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public UserResponse createUser(UserDto userDto) {
        return userManagementService.createUser(userDto);
    }

    @Override
    public UserResponse getUser(Long id) {
        return userManagementService.getUser(id);
    }

    @Override
    public UserResponse updateUser(Long id, UserDto userDto) {
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
    public List<UserResponse> getAllUsers() {
        return userManagementService.getAllUsers();
    }

    @Override
    public boolean validateUser(String email, String password) {
        return authenticationService.validateUser(email, password);
    }
}
