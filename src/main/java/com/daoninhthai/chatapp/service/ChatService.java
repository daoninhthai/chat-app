package com.daoninhthai.chatapp.service;

import com.daoninhthai.chatapp.dto.ChatMessageDto;
import com.daoninhthai.chatapp.entity.ChatMessage;
import com.daoninhthai.chatapp.entity.ChatRoom;
import com.daoninhthai.chatapp.entity.User;
import com.daoninhthai.chatapp.repository.ChatMessageRepository;
import com.daoninhthai.chatapp.repository.ChatRoomRepository;
import com.daoninhthai.chatapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserRepository userRepository;

    public void saveMessage(ChatMessageDto messageDto) {
        try {
            User sender = userRepository.findByUsername(messageDto.getSender())
                    .orElse(null);

            ChatRoom chatRoom = chatRoomRepository.findById(messageDto.getRoomId())
                    .orElse(null);

            if (sender == null || chatRoom == null) {
                logger.warn("Khong tim thay sender hoac chat room, bo qua luu tin nhan");
                return;
            }

            ChatMessage message = ChatMessage.builder()
                    .content(messageDto.getContent())
                    .sender(sender)
                    .chatRoom(chatRoom)
                    .timestamp(LocalDateTime.now())
                    .messageType(ChatMessage.MessageType.valueOf(messageDto.getType().name()))
                    .build();

            chatMessageRepository.save(message);
        } catch (Exception e) {
            logger.error("Loi khi luu tin nhan: {}", e.getMessage());
        }
    }

    public List<ChatMessageDto> getMessageHistory(Long roomId) {
        List<ChatMessage> messages = chatMessageRepository
                .findTop50ByChatRoomIdOrderByTimestampDesc(roomId);

        // dao nguoc lai de hien thi theo thu tu thoi gian
        Collections.reverse(messages);

        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ChatMessageDto convertToDto(ChatMessage message) {
        return ChatMessageDto.builder()
                .content(message.getContent())
                .sender(message.getSender().getUsername())
                .type(ChatMessageDto.MessageType.valueOf(message.getMessageType().name()))
                .roomId(message.getChatRoom().getId())
                .timestamp(message.getTimestamp())
                .build();
    }
}
