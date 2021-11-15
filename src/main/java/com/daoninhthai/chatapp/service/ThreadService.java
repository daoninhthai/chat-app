package com.daoninhthai.chatapp.service;

import com.daoninhthai.chatapp.dto.ChatMessageDto;
import com.daoninhthai.chatapp.dto.ThreadResponse;
import com.daoninhthai.chatapp.entity.ChatMessage;
import com.daoninhthai.chatapp.entity.ChatRoom;
import com.daoninhthai.chatapp.entity.MessageThread;
import com.daoninhthai.chatapp.entity.User;
import com.daoninhthai.chatapp.repository.ChatMessageRepository;
import com.daoninhthai.chatapp.repository.ChatRoomRepository;
import com.daoninhthai.chatapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ThreadService {

    private static final Logger logger = LoggerFactory.getLogger(ThreadService.class);

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private MessageService messageService;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public ChatMessageDto replyToMessage(Long parentMessageId, String senderUsername,
                                          String content, Long roomId) {
        // Verify parent message exists
        ChatMessage parentMessage = chatMessageRepository.findById(parentMessageId)
                .orElseThrow(() -> new RuntimeException("Parent message not found: " + parentMessageId));

        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + senderUsername));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found: " + roomId));

        // Create the reply message
        ChatMessage reply = ChatMessage.builder()
                .content(content)
                .sender(sender)
                .chatRoom(chatRoom)
                .timestamp(LocalDateTime.now())
                .messageType(ChatMessage.MessageType.CHAT)
                .parentMessageId(parentMessageId)
                .isThread(true)
                .build();

        ChatMessage savedReply = chatMessageRepository.save(reply);

        // Mark parent as thread if not already
        if (!parentMessage.isThread()) {
            parentMessage.setThread(true);
            chatMessageRepository.save(parentMessage);
        }

        // Update or create thread metadata
        MessageThread thread = findOrCreateThread(parentMessageId);
        thread.setReplyCount(thread.getReplyCount() + 1);
        thread.setLastReplyAt(LocalDateTime.now());
        entityManager.merge(thread);

        logger.info("Reply created for message {} by user {}", parentMessageId, senderUsername);
        return messageService.convertToDto(savedReply);
    }

    public ThreadResponse getThread(Long parentMessageId) {
        ChatMessage parentMessage = chatMessageRepository.findById(parentMessageId)
                .orElseThrow(() -> new RuntimeException("Parent message not found: " + parentMessageId));

        List<ChatMessage> replies = chatMessageRepository.findByParentMessageIdOrderByTimestampAsc(parentMessageId);

        MessageThread thread = findOrCreateThread(parentMessageId);

        return ThreadResponse.builder()
                .parentMessage(messageService.convertToDto(parentMessage))
                .replies(replies.stream()
                        .map(messageService::convertToDto)
                        .collect(Collectors.toList()))
                .replyCount(thread.getReplyCount())
                .lastReplyAt(thread.getLastReplyAt())
                .build();
    }

    public List<ChatMessageDto> getThreadReplies(Long parentMessageId) {
        List<ChatMessage> replies = chatMessageRepository.findByParentMessageIdOrderByTimestampAsc(parentMessageId);
        return replies.stream()
                .map(messageService::convertToDto)
                .collect(Collectors.toList());
    }

    private MessageThread findOrCreateThread(Long parentMessageId) {
        List<MessageThread> threads = entityManager
                .createQuery("SELECT t FROM MessageThread t WHERE t.parentMessageId = :parentId", MessageThread.class)
                .setParameter("parentId", parentMessageId)
                .getResultList();

        if (!threads.isEmpty()) {
            return threads.get(0);
        }

        MessageThread thread = MessageThread.builder()
                .parentMessageId(parentMessageId)
                .replyCount(0)
                .lastReplyAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        entityManager.persist(thread);
        return thread;
    }
}
