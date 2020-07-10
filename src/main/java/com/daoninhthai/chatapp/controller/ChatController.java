package com.daoninhthai.chatapp.controller;

import com.daoninhthai.chatapp.dto.ChatMessageDto;
import com.daoninhthai.chatapp.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

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
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        headerAccessor.getSessionAttributes().put("roomId", roomId);

        chatMessage.setRoomId(roomId);
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setType(ChatMessageDto.MessageType.JOIN);
        chatMessage.setContent(chatMessage.getSender() + " da tham gia phong chat!");

        return chatMessage;
    }
}
