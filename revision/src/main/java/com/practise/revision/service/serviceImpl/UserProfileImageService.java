package com.practise.revision.service.serviceImpl;

import com.practise.revision.entity.User;
import com.practise.revision.entity.UserProfileImage;
import com.practise.revision.repository.UserProfileImageRepository;
import com.practise.revision.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;


@Service
public class UserProfileImageService {
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final UserRepository userRepository;
    private final UserProfileImageRepository userProfileImageRepository;
    private static final Logger log = LoggerFactory.getLogger(UserProfileImageService.class);

    public UserProfileImageService(UserRepository userRepository, UserProfileImageRepository userProfileImageRepository) {
        this.userRepository = userRepository;
        this.userProfileImageRepository = userProfileImageRepository;

    }

    public  void uploadProfileImage(MultipartFile file){
        log.info("request reached uploadProfileImageMethod");
        if(file==null || file.isEmpty()){
            throw  new IllegalArgumentException("File must not be empty");

        }
        String contentType= file.getContentType();
        if(contentType==null || !contentType.startsWith("image/")){
            throw  new IllegalArgumentException("Only image files are allowed");
        }
        if (file.getSize()> MAX_FILE_SIZE){
            throw  new IllegalArgumentException("file size exceeds the "+ MAX_FILE_SIZE+" limit");

        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email);
        if(user==null){
            throw new IllegalStateException("please login to upload");
        }
        try {
            UserProfileImage image = userProfileImageRepository.findById(user.getId())
                .orElseGet(() -> {
                    UserProfileImage newImage = new UserProfileImage();
                    newImage.setUser(user);
                    return newImage;
                });

            image.setImageData(file.getBytes());
            image.setContentType(file.getContentType());
            image.setFileSize(file.getSize());
            image.setUploadedAt(Instant.now());
            userProfileImageRepository.save(image);

        } catch (IOException e) {
            throw new RuntimeException("Failed to process image", e);
        }
    }
}
