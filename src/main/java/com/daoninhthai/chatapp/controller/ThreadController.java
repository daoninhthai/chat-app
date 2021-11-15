package com.daoninhthai.chatapp.controller;

import com.daoninhthai.chatapp.dto.ChatMessageDto;
import com.daoninhthai.chatapp.dto.ThreadResponse;
import com.daoninhthai.chatapp.service.ThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class ThreadController {

    @Autowired
    private ThreadService threadService;

    @PostMapping("/{messageId}/reply")
    public ResponseEntity<?> replyToMessage(
            @PathVariable Long messageId,
            @RequestParam("sender") String sender,
            @RequestParam("content") String content,
            @RequestParam("roomId") Long roomId) {
        try {
            ChatMessageDto reply = threadService.replyToMessage(messageId, sender, content, roomId);
            return ResponseEntity.ok(reply);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{messageId}/thread")
    public ResponseEntity<?> getThread(@PathVariable Long messageId) {
        try {
            ThreadResponse thread = threadService.getThread(messageId);
            return ResponseEntity.ok(thread);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{messageId}/replies")
    public ResponseEntity<List<ChatMessageDto>> getThreadReplies(@PathVariable Long messageId) {
        List<ChatMessageDto> replies = threadService.getThreadReplies(messageId);
        return ResponseEntity.ok(replies);
    }
}
