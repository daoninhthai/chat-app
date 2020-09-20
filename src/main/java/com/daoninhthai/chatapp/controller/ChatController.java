package com.daoninhthai.chatapp.controller;

import com.daoninhthai.chatapp.dto.ChatMessageDto;
import com.daoninhthai.chatapp.service.ChatService;
import com.daoninhthai.chatapp.service.OnlineUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Set;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private OnlineUserService onlineUserService;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/room.{roomId}")
    public ChatMessageDto sendMessage(@DestinationVariable Long roomId,
                                       @Payload ChatMessageDto chatMessage) {
        chatMessage.setRoomId(roomId);
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setType(ChatMessageDto.MessageType.CHAT);

        // luu tin nhan vao database
        chatService.saveMessage(chatMessage);

        return chatMessage;
    }

    @MessageMapping("/chat.addUser/{roomId}")
    @SendTo("/topic/room.{roomId}")
    public ChatMessageDto addUser(@DestinationVariable Long roomId,
                                   @Payload ChatMessageDto chatMessage,
                                   SimpMessageHeaderAccessor headerAccessor) {
        // luu username vao websocket session
        String username = chatMessage.getSender();
        String sessionId = headerAccessor.getSessionId();

        headerAccessor.getSessionAttributes().put("username", username);
        headerAccessor.getSessionAttributes().put("roomId", roomId);

        // them user vao danh sach online
        onlineUserService.addUser(sessionId, username, roomId);

        // gui cap nhat danh sach user online cho phong
        Set<String> onlineUsers = onlineUserService.getOnlineUsers(roomId);
        messagingTemplate.convertAndSend("/topic/room." + roomId + ".users", onlineUsers);

        chatMessage.setRoomId(roomId);
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setType(ChatMessageDto.MessageType.JOIN);
        chatMessage.setContent(username + " da tham gia phong chat!");

        return chatMessage;
    }
}
