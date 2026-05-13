package com.practise.userservice.utils;

import com.practise.userservice.dto.UserResponse;
import com.practise.userservice.entity.User;

public class UserResponseMessage {
    public static UserResponse buildUserResponse(User user, String message) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setMessage(message);
        return response;
    }

}
