package com.daoninhthai.chatapp.controller;

import com.daoninhthai.chatapp.entity.ChatRoom;
import com.daoninhthai.chatapp.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    @GetMapping
    public ResponseEntity<List<ChatRoom>> getAllRooms() {
        return ResponseEntity.ok(chatRoomService.getRooms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatRoom> getRoom(@PathVariable Long id) {
        return chatRoomService.getRoom(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ChatRoom> createRoom(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String description = request.get("description");
        String creator = request.get("creator");

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ChatRoom room = chatRoomService.createRoom(name, description, creator);
        return ResponseEntity.ok(room);
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<Map<String, String>> joinRoom(@PathVariable Long id,
                                                         @RequestBody Map<String, String> request) {
        String username = request.get("username");
        chatRoomService.joinRoom(id, username);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Da tham gia phong thanh cong");
        return ResponseEntity.ok(response);
    }
}
