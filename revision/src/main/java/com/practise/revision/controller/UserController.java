package com.practise.revision.controller;

import com.practise.revision.dto.OrderDto;
import com.practise.revision.dto.UserDto;
import com.practise.revision.dto.UserEvent;
import com.practise.revision.producer.UserEventProducer;
import com.practise.revision.service.OrderService;
import com.practise.revision.service.UserManagementService;
import com.practise.revision.service.serviceImpl.UserProfileImageService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class UserController {
    
    private final UserManagementService userManagementService;
    private final OrderService orderService;
    private final UserEventProducer userEventProducer;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserProfileImageService userProfileImageService;

    public UserController(UserManagementService userManagementService,
                          OrderService orderService,
                          UserEventProducer userEventProducer, UserProfileImageService userProfileImageService) {
        this.userManagementService = userManagementService;
        this.orderService = orderService;
        this.userEventProducer = userEventProducer;

        this.userProfileImageService = userProfileImageService;
    }


    @PostMapping("/register")
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserDto userDto) {
        UserDto createdUser = userManagementService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id){
        UserDto user = userManagementService.getUser(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/publish")
    public ResponseEntity<Map<String, String>> publishUserEvent(@Valid @RequestBody UserEvent event) {
        userEventProducer.sendUserCreatedEvent(event);
        return ResponseEntity.ok(Map.of("message", "Event published successfully", "user", event.getName()));
    }

    @PutMapping("/updateUser")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserDto> updateUser(@Valid @RequestBody UserDto userDto){
        UserDto updatedUser = userManagementService.updateUser( userDto);
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/deleteUser/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        userManagementService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/orders")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<OrderDto>> getUserOrders(@PathVariable Long id) {
        List<OrderDto> orders = orderService.getUserOrders(id);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/users/v1/allUsers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userManagementService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/profile-image")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        log.info("Uploading profile image for user: {}", file.getOriginalFilename());
        userProfileImageService.uploadProfileImage(file);
        return ResponseEntity.ok(Map.of("message", "Profile image uploaded successfully"));
    }

}
