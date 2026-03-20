package com.practise.revision.service;

import com.practise.revision.dto.UserDto;

import java.util.List;

public interface UserManagementService {
    UserDto createUser(UserDto userDto);
    UserDto getUser(Long id);
    UserDto updateUser(UserDto userDto);
    void deleteUser(Long id);
    List<UserDto> getAllUsers();
}
