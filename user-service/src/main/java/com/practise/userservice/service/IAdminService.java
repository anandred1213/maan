package com.practise.userservice.service;

import com.practise.userservice.dto.UserDto;
import com.practise.userservice.dto.UserResponse;

public interface IAdminService {

    public UserResponse createAdmin(UserDto userDto);
}
