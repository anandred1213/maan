package com.practise.userservice.controller;

import com.practise.userservice.dto.FileUploadRequest;
import com.practise.userservice.dto.OrderDto;
import com.practise.userservice.dto.UserDto;
import com.practise.userservice.dto.UserEvent;
import com.practise.userservice.dto.UserResponse;
import com.practise.userservice.producer.UserEventProducer;
import com.practise.userservice.security.CustomUserDetails;
import com.practise.userservice.service.OrderService;
import com.practise.userservice.service.UserManagementService;
import com.practise.userservice.service.serviceImpl.UserProfileImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, maxAge = 3600)
@Tag(name = "User", description = "User management endpoints")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserManagementService userManagementService;
    private final OrderService orderService;
    private final UserEventProducer userEventProducer;
    private final UserProfileImageService userProfileImageService;

    public UserController(UserManagementService userManagementService,
                          OrderService orderService,
                          UserEventProducer userEventProducer,
                          UserProfileImageService userProfileImageService) {
        this.userManagementService = userManagementService;
        this.orderService = orderService;
        this.userEventProducer = userEventProducer;
        this.userProfileImageService = userProfileImageService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserDto userDto) {
        UserResponse createdUser = userManagementService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    /**
     * Logged-in user profile
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current logged-in user")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserResponse response =
                userManagementService.getUser(userDetails.getUser().getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.getUser(id));
    }

    @PostMapping("/publish")
    @Operation(summary = "Publish user event to Kafka")
    public ResponseEntity<Map<String, String>> publishUserEvent(@Valid @RequestBody UserEvent event) {
        userEventProducer.sendUserCreatedEvent(event);
        return ResponseEntity.ok(Map.of("message", "Event published successfully", "user", event.getName()));
    }

    @PutMapping("/updateUser")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update user")
    public ResponseEntity<UserResponse> updateUser(@RequestBody UserDto userDto) {
        log.info("Update request received - id: {}, name: {}, email: {}", userDto.getId(), userDto.getName(), userDto.getEmail());
        return ResponseEntity.ok(userManagementService.updateUser(userDto));
    }

    @DeleteMapping("/deleteUser/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Delete user by ID")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userManagementService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/orders")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get orders for a user")
    public ResponseEntity<List<OrderDto>> getUserOrders(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getUserOrders(id));
    }

    @GetMapping("/v1/allUsers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (Admin only)")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userManagementService.getAllUsers());
    }

    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Upload profile image",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = FileUploadRequest.class))))
    public ResponseEntity<Map<String, String>> uploadProfileImage(
            @RequestPart("file") MultipartFile file) {
        log.info("Uploading profile image: {}", file.getOriginalFilename());
        userProfileImageService.uploadProfileImage(file);
        return ResponseEntity.ok(Map.of("message", "Profile image uploaded successfully"));
    }

    @GetMapping("/profile-image")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get profile image")
    public ResponseEntity<byte[]> getProfileImage() {
        var image = userProfileImageService.getProfileImage();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .body(image.getImageData());
    }
}
