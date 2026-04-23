package com.practise.userservice.service.serviceImpl;

import com.practise.userservice.entity.User;
import com.practise.userservice.entity.UserProfileImage;
import com.practise.userservice.repository.UserProfileImageRepository;
import com.practise.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;

@Service
public class UserProfileImageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Logger log = LoggerFactory.getLogger(UserProfileImageService.class);

    private final UserRepository userRepository;
    private final UserProfileImageRepository userProfileImageRepository;

    public UserProfileImageService(UserRepository userRepository,
                                   UserProfileImageRepository userProfileImageRepository) {
        this.userRepository = userRepository;
        this.userProfileImageRepository = userProfileImageRepository;
    }

    @Transactional
    public void uploadProfileImage(MultipartFile file) {
        log.info("Request reached uploadProfileImage");

        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File must not be empty");

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/"))
            throw new IllegalArgumentException("Only image files are allowed");

        if (file.getSize() > MAX_FILE_SIZE)
            throw new IllegalArgumentException("File size exceeds " + (MAX_FILE_SIZE / 1024 / 1024) + "MB limit");

        User user = getAuthenticatedUser();

        try {
            UserProfileImage image = userProfileImageRepository.findById(user.getId())
                    .orElseGet(() -> {
                        UserProfileImage newImage = new UserProfileImage();
                        newImage.setUser(user);
                        return newImage;
                    });

            image.setImageData(file.getBytes());
            image.setContentType(contentType);
            image.setFileSize(file.getSize());
            image.setUploadedAt(Instant.now());
            userProfileImageRepository.save(image);
            log.info("Profile image saved for user: {}", user.getEmail());

        } catch (IOException e) {
            throw new RuntimeException("Failed to process image", e);
        }
    }

    @Transactional(readOnly = true)
    public UserProfileImage getProfileImage() {
        User user = getAuthenticatedUser();
        return userProfileImageRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("No profile image found for user"));
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email);
        if (user == null)
            throw new IllegalStateException("Please login to perform this action");
        return user;
    }
}
