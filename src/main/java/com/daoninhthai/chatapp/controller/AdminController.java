package com.daoninhthai.chatapp.controller;

import com.daoninhthai.chatapp.dto.SystemStats;
import com.daoninhthai.chatapp.entity.ChatRoom;
import com.daoninhthai.chatapp.entity.User;
import com.daoninhthai.chatapp.entity.UserBan;
import com.daoninhthai.chatapp.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<SystemStats> getSystemStats() {
        SystemStats stats = adminService.getSystemStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<Map<String, Object>>> getAllRooms() {
        List<ChatRoom> rooms = adminService.getAllRooms();

        List<Map<String, Object>> response = rooms.stream()
                .map(room -> {
                    Map<String, Object> roomData = new HashMap<>();
                    roomData.put("id", room.getId());
                    roomData.put("name", room.getName());
                    roomData.put("description", room.getDescription());
                    roomData.put("createdAt", room.getCreatedAt() != null
                            ? room.getCreatedAt().toString() : null);
                    roomData.put("messageCount", adminService.getRoomMessageCount(room.getId()));
                    return roomData;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<User> users = adminService.getAllUsers();

        List<Map<String, Object>> response = users.stream()
                .map(user -> {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("id", user.getId());
                    userData.put("username", user.getUsername());
                    userData.put("email", user.getEmail());
                    userData.put("displayName", user.getDisplayName());
                    userData.put("createdAt", user.getCreatedAt() != null
                            ? user.getCreatedAt().toString() : null);
                    userData.put("isBanned", adminService.isUserBanned(user.getId()));
                    return userData;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{userId}/ban")
    public ResponseEntity<?> banUser(
            @PathVariable Long userId,
            @RequestParam("adminId") Long adminId,
            @RequestParam("reason") String reason,
            @RequestParam(value = "expiresAt", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expiresAt) {
        try {
            UserBan ban = adminService.banUser(userId, adminId, reason, expiresAt);

            Map<String, Object> response = new HashMap<>();
            response.put("id", ban.getId());
            response.put("userId", ban.getUserId());
            response.put("reason", ban.getReason());
            response.put("bannedBy", ban.getBannedBy());
            response.put("bannedAt", ban.getBannedAt().toString());
            response.put("expiresAt", ban.getExpiresAt() != null
                    ? ban.getExpiresAt().toString() : "permanent");

            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/users/{userId}/unban")
    public ResponseEntity<Map<String, String>> unbanUser(@PathVariable Long userId) {
        try {
            adminService.unbanUser(userId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "User " + userId + " unbanned successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Map<String, String>> deleteRoom(@PathVariable Long roomId) {
        try {
            adminService.deleteRoom(roomId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Room " + roomId + " deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/bans")
    public ResponseEntity<List<UserBan>> getActiveBans() {
        List<UserBan> bans = adminService.getActiveBans();
        return ResponseEntity.ok(bans);
    }
}
