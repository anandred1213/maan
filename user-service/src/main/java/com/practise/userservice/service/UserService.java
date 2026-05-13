package com.practise.userservice.service;

import com.practise.userservice.dto.OrderDto;
import com.practise.userservice.dto.UserDto;
import com.practise.userservice.dto.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserDto userDto);
    UserResponse getUser(Long id);
    UserResponse updateUser(Long id, UserDto userDto);
    void deleteUser(Long id);
    List<OrderDto> getUserOrders(Long userId);
    List<UserResponse> getAllUsers();
    boolean validateUser(String email, String password);
}
