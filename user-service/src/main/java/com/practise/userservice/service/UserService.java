package com.practise.userservice.service;

import com.practise.userservice.dto.OrderDto;
import com.practise.userservice.dto.UserDto;

import java.util.List;


public interface UserService {
    UserDto createUser(UserDto userDto);
    UserDto getUser(Long id);
    UserDto updateUser(Long id, UserDto userDto);
    void deleteUser(Long id);
    List<OrderDto> getUserOrders(Long userId);
    List<UserDto> getAllUsers();
    boolean validateUser(String email, String password);
}
