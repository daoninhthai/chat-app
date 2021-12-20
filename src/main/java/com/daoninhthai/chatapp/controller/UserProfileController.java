package com.daoninhthai.chatapp.controller;

import com.daoninhthai.chatapp.dto.UpdateProfileRequest;
import com.daoninhthai.chatapp.dto.UserProfileResponse;
import com.daoninhthai.chatapp.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(
            @RequestParam("userId") Long userId) {
        try {
            UserProfileResponse profile = userProfileService.getProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> getProfileByUsername(
            @PathVariable String username) {
        try {
            UserProfileResponse profile = userProfileService.getProfileByUsername(username);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestParam("userId") Long userId,
            @RequestBody UpdateProfileRequest request) {
        try {
            UserProfileResponse updated = userProfileService.updateProfile(userId, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("userId") Long userId,
            @RequestParam("avatar") MultipartFile avatarFile) {
        try {
            UserProfileResponse updated = userProfileService.uploadAvatar(userId, avatarFile);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload avatar");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
