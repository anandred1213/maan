package com.practise.userservice.controller;

import com.practise.userservice.dto.UserDto;
import com.practise.userservice.dto.UserResponse;
import com.practise.userservice.service.IAdminService;
import com.practise.userservice.service.serviceImpl.AdminServiceImpl;


import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/maan/admin")
public class AdminController{
    private final IAdminService adminService;

    public AdminController(IAdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/adregister")
    public ResponseEntity<UserResponse> registerAdmin(@Valid @RequestBody UserDto userDto) {
        UserResponse response = adminService.createAdmin(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
