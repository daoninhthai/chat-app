package com.daoninhthai.chatapp.controller;

import com.daoninhthai.chatapp.entity.PinnedMessage;
import com.daoninhthai.chatapp.service.PinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms/{roomId}/pins")
public class PinController {

    @Autowired
    private PinService pinService;

    @PostMapping
    public ResponseEntity<?> pinMessage(
            @PathVariable Long roomId,
            @RequestParam("messageId") Long messageId,
            @RequestParam("userId") Long userId) {
        try {
            PinnedMessage pinned = pinService.pinMessage(messageId, roomId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("id", pinned.getId());
            response.put("messageId", pinned.getMessageId());
            response.put("roomId", pinned.getRoomId());
            response.put("pinnedBy", pinned.getPinnedBy());
            response.put("pinnedAt", pinned.getPinnedAt().toString());

            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Map<String, String>> unpinMessage(
            @PathVariable Long roomId,
            @PathVariable Long messageId) {
        pinService.unpinMessage(messageId, roomId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Message unpinned successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PinnedMessage>> getPinnedMessages(
            @PathVariable Long roomId) {
        List<PinnedMessage> pinnedMessages = pinService.getPinnedMessages(roomId);
        return ResponseEntity.ok(pinnedMessages);
    }
}
