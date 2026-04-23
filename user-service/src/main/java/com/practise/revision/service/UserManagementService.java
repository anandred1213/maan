package com.practise.userservice.service;

import com.practise.userservice.dto.UserDto;

import java.util.List;

public interface UserManagementService {
    UserDto createUser(UserDto userDto);
    UserDto getUser(Long id);
    UserDto updateUser(UserDto userDto);
    void deleteUser(Long id);
    List<UserDto> getAllUsers();
}
