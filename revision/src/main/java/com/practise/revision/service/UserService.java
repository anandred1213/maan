package com.practise.revision.service;

import com.practise.revision.dto.OrderDto;
import com.practise.revision.dto.UserDto;

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
