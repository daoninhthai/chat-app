package com.daoninhthai.chatapp.config;

import com.daoninhthai.chatapp.dto.ChatMessageDto;
import com.daoninhthai.chatapp.service.ChatService;
import com.daoninhthai.chatapp.service.OnlineUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.Set;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private ChatService chatService;

    @Autowired
    private OnlineUserService onlineUserService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("Nhan duoc ket noi WebSocket moi");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        Long roomId = (Long) headerAccessor.getSessionAttributes().get("roomId");
        String sessionId = headerAccessor.getSessionId();

        if (username != null) {
            logger.info("User {} da ngat ket noi (session: {})", username, sessionId);

            // cap nhat trang thai online
            onlineUserService.removeUser(sessionId);

            // xu ly khi user mat ket noi
            chatService.handleUserDisconnect(username, roomId);

            // gui thong bao cho phong chat
            if (roomId != null) {
                ChatMessageDto chatMessage = ChatMessageDto.builder()
                        .type(ChatMessageDto.MessageType.LEAVE)
                        .sender(username)
                        .content(username + " da roi phong chat.")
                        .roomId(roomId)
                        .timestamp(LocalDateTime.now())
                        .build();

                messagingTemplate.convertAndSend("/topic/room." + roomId, chatMessage);

                // gui cap nhat danh sach online
                Set<String> onlineUsers = onlineUserService.getOnlineUsers(roomId);
                messagingTemplate.convertAndSend("/topic/room." + roomId + ".users", onlineUsers);
            }
        }
    }
}
