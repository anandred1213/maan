package com.practise.userservice.service;

import com.practise.userservice.dto.UserDto;
import com.practise.userservice.dto.UserResponse;

import java.util.List;

public interface UserManagementService {
    UserResponse createUser(UserDto userDto);
    UserResponse getUser(Long id);
    UserResponse updateUser(UserDto userDto);
    void deleteUser(Long id);
    List<UserResponse> getAllUsers();
}
