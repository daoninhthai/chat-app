package com.daoninhthai.chatapp.service;

import com.daoninhthai.chatapp.dto.ChatMessageDto;
import com.daoninhthai.chatapp.entity.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service chinh cho chat, uy quyen cho cac service con
 */
@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private MessageService messageService;

    public void saveMessage(ChatMessageDto messageDto) {
        messageService.save(messageDto);
    }

    public List<ChatMessageDto> getMessageHistory(Long roomId) {
        return messageService.getRecentMessages(roomId);
    }

    public void handleUserDisconnect(String username, Long roomId) {
        if (roomId == null) {
            logger.warn("User {} disconnect nhung khong co roomId", username);
            return;
        }

        try {
            ChatMessageDto leaveMessage = ChatMessageDto.builder()
                    .content(username + " da roi phong chat.")
                    .sender(username)
                    .type(ChatMessageDto.MessageType.LEAVE)
                    .roomId(roomId)
                    .build();

            messageService.save(leaveMessage);
            logger.info("Da luu thong bao disconnect cho user: {}", username);
        } catch (Exception e) {
            logger.error("Loi khi xu ly disconnect cua user {}: {}", username, e.getMessage());
        }
    }
}
