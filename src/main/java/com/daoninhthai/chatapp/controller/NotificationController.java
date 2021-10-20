package com.daoninhthai.chatapp.controller;

import com.daoninhthai.chatapp.dto.NotificationPreferenceDto;
import com.daoninhthai.chatapp.entity.NotificationPreference;
import com.daoninhthai.chatapp.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferenceDto> getPreferences(
            @RequestParam("userId") Long userId) {
        NotificationPreference prefs = notificationService.getOrCreatePreferences(userId);
        return ResponseEntity.ok(notificationService.toDto(prefs));
    }

    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreferenceDto> updatePreferences(
            @RequestParam("userId") Long userId,
            @RequestBody NotificationPreferenceDto dto) {
        NotificationPreference updated = notificationService.updatePreferences(userId, dto);
        return ResponseEntity.ok(notificationService.toDto(updated));
    }

    @PostMapping("/mute/{roomId}")
    public ResponseEntity<Map<String, String>> muteRoom(
            @PathVariable Long roomId,
            @RequestParam("userId") Long userId) {
        notificationService.muteRoom(userId, roomId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Room " + roomId + " muted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/unmute/{roomId}")
    public ResponseEntity<Map<String, String>> unmuteRoom(
            @PathVariable Long roomId,
            @RequestParam("userId") Long userId) {
        notificationService.unmuteRoom(userId, roomId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Room " + roomId + " unmuted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mute-all")
    public ResponseEntity<Map<String, String>> muteAll(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "duration", defaultValue = "60") int durationMinutes) {
        notificationService.muteAll(userId, durationMinutes);

        Map<String, String> response = new HashMap<>();
        response.put("message", "All notifications muted for " + durationMinutes + " minutes");
        return ResponseEntity.ok(response);
    }
}
