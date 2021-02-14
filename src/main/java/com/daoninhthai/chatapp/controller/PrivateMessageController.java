package com.daoninhthai.chatapp.controller;

import com.daoninhthai.chatapp.entity.PrivateMessage;
import com.daoninhthai.chatapp.service.PrivateMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class PrivateMessageController {

    @Autowired
    private PrivateMessageService privateMessageService;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    /**
     * Lay lich su tin nhan voi 1 user
     * GET /api/messages/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getConversation(
            @PathVariable Long userId,
            Principal principal) {

        String username = principal.getName();
        List<PrivateMessage> messages = privateMessageService.getConversation(username, userId);

        List<Map<String, Object>> response = messages.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Gui tin nhan rieng
     * POST /api/messages/{userId}
     */
    @PostMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body,
            Principal principal) {

        String username = principal.getName();
        String content = body.get("content");

        PrivateMessage message = privateMessageService.sendMessage(username, userId, content);

        // gui tin nhan qua WebSocket cho nguoi nhan
        Map<String, Object> messageData = convertToMap(message);
        messagingTemplate.convertAndSendToUser(
                message.getReceiver().getUsername(),
                "/queue/private",
                messageData
        );

        return ResponseEntity.ok(messageData);
    }

    /**
     * Danh dau tin nhan da doc
     * PUT /api/messages/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id) {
        privateMessageService.markAsRead(id);

        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "Da danh dau tin nhan la da doc");

        return ResponseEntity.ok(response);
    }

    /**
     * Lay so tin nhan chua doc
     * GET /api/messages/unread/count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Principal principal) {
        long count = privateMessageService.getUnreadCount(principal.getName());

        Map<String, Long> response = new HashMap<>();
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    /**
     * Xu ly tin nhan rieng qua WebSocket
     * Client gui den /app/chat.private
     */
    @MessageMapping("/chat.private")
    public void handlePrivateMessage(@Payload Map<String, Object> payload) {
        String senderUsername = (String) payload.get("sender");
        Long receiverId = Long.valueOf(payload.get("receiverId").toString());
        String content = (String) payload.get("content");

        PrivateMessage message = privateMessageService.sendMessage(
                senderUsername, receiverId, content);

        Map<String, Object> messageData = convertToMap(message);

        // gui cho nguoi nhan
        messagingTemplate.convertAndSendToUser(
                message.getReceiver().getUsername(),
                "/queue/private",
                messageData
        );

        // gui lai cho nguoi gui de xac nhan
        messagingTemplate.convertAndSendToUser(
                senderUsername,
                "/queue/private",
                messageData
        );
    }

    /**
     * Chuyen doi PrivateMessage entity sang Map de tra ve client
     */
    private Map<String, Object> convertToMap(PrivateMessage message) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", message.getId());
        map.put("senderId", message.getSender().getId());
        map.put("senderUsername", message.getSender().getUsername());
        map.put("receiverId", message.getReceiver().getId());
        map.put("receiverUsername", message.getReceiver().getUsername());
        map.put("content", message.getContent());
        map.put("timestamp", message.getTimestamp().toString());
        map.put("read", message.isRead());
        return map;
    }
}
