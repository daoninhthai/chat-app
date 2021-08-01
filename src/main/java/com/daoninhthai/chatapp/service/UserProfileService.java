package com.daoninhthai.chatapp.service;

import com.daoninhthai.chatapp.dto.UpdateProfileRequest;
import com.daoninhthai.chatapp.dto.UserProfileResponse;
import com.daoninhthai.chatapp.entity.User;
import com.daoninhthai.chatapp.repository.ChatMessageRepository;
import com.daoninhthai.chatapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserProfileService {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        long messageCount = chatMessageRepository.countBySenderId(userId);

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .joinedAt(user.getCreatedAt())
                .messageCount(messageCount)
                .build();
    }

    public UserProfileResponse getProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        long messageCount = chatMessageRepository.countBySenderId(user.getId());

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .joinedAt(user.getCreatedAt())
                .messageCount(messageCount)
                .build();
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (request.getDisplayName() != null && !request.getDisplayName().trim().isEmpty()) {
            user.setDisplayName(request.getDisplayName().trim());
        }

        if (request.getBio() != null) {
            if (request.getBio().length() > 500) {
                throw new IllegalArgumentException("Bio must not exceed 500 characters");
            }
            user.setBio(request.getBio().trim());
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl().trim());
        }

        userRepository.save(user);
        logger.info("Profile updated for user: {}", user.getUsername());

        return getProfile(userId);
    }

    @Transactional
    public UserProfileResponse uploadAvatar(Long userId, MultipartFile avatarFile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Validate that it's an image file
        String contentType = avatarFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Avatar must be an image file");
        }

        // Delete old avatar if exists
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            try {
                String oldFilename = user.getAvatarUrl().substring(
                        user.getAvatarUrl().lastIndexOf('/') + 1);
                fileStorageService.deleteFile(oldFilename);
            } catch (Exception e) {
                logger.warn("Could not delete old avatar: {}", e.getMessage());
            }
        }

        // Store new avatar
        var attachment = fileStorageService.storeFile(avatarFile, null);
        user.setAvatarUrl(attachment.getDownloadUrl());
        userRepository.save(user);

        logger.info("Avatar uploaded for user: {}", user.getUsername());
        return getProfile(userId);
    }
}
