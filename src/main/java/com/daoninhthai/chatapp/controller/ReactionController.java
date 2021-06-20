package com.daoninhthai.chatapp.controller;

import com.daoninhthai.chatapp.entity.MessageReaction;
import com.daoninhthai.chatapp.entity.MessageReaction.ReactionType;
import com.daoninhthai.chatapp.service.ReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class ReactionController {

    @Autowired
    private ReactionService reactionService;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @PostMapping("/{messageId}/reactions")
    public ResponseEntity<Map<String, Object>> addReaction(
            @PathVariable Long messageId,
            @RequestParam("userId") Long userId,
            @RequestParam("type") ReactionType reactionType,
            @RequestParam(value = "roomId", required = false) Long roomId) {

        MessageReaction reaction = reactionService.toggleReaction(messageId, userId, reactionType);

        Map<String, Object> response = new HashMap<>();
        response.put("messageId", messageId);
        response.put("userId", userId);
        response.put("reactionType", reactionType);
        response.put("action", reaction != null ? "added" : "removed");
        response.put("counts", reactionService.getReactionCounts(messageId));

        // Broadcast reaction update via WebSocket
        if (roomId != null) {
            messagingTemplate.convertAndSend(
                    "/topic/room." + roomId + ".reactions", response);
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{messageId}/reactions")
    public ResponseEntity<Map<String, String>> removeReaction(
            @PathVariable Long messageId,
            @RequestParam("userId") Long userId,
            @RequestParam("type") ReactionType reactionType) {

        reactionService.removeReaction(messageId, userId, reactionType);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Reaction removed successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{messageId}/reactions")
    public ResponseEntity<Map<String, Object>> getReactions(@PathVariable Long messageId) {
        List<MessageReaction> reactions = reactionService.getReactions(messageId);
        Map<ReactionType, Long> counts = reactionService.getReactionCounts(messageId);

        Map<String, Object> response = new HashMap<>();
        response.put("messageId", messageId);
        response.put("reactions", reactions);
        response.put("counts", counts);
        response.put("totalReactions", reactions.size());

        return ResponseEntity.ok(response);
    }
}
