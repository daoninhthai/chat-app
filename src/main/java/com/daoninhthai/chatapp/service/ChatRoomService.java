package com.daoninhthai.chatapp.service;

import com.daoninhthai.chatapp.entity.ChatRoom;
import com.daoninhthai.chatapp.entity.ChatRoomMember;
import com.daoninhthai.chatapp.entity.User;
import com.daoninhthai.chatapp.repository.ChatRoomRepository;
import com.daoninhthai.chatapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Service
public class ChatRoomService {

    private static final Logger logger = LoggerFactory.getLogger(ChatRoomService.class);

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public ChatRoom createRoom(String name, String description, String creatorUsername) {
        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new RuntimeException("Khong tim thay user: " + creatorUsername));

        ChatRoom chatRoom = ChatRoom.builder()
                .name(name)
                .description(description)
                .createdBy(creator)
                .build();

        chatRoom = chatRoomRepository.save(chatRoom);

        // tu dong them nguoi tao vao phong
        joinRoom(chatRoom.getId(), creatorUsername);

        logger.info("User {} da tao phong chat: {}", creatorUsername, name);
        return chatRoom;
    }

    public List<ChatRoom> getRooms() {
        return chatRoomRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<ChatRoom> getRoom(Long id) {
        return chatRoomRepository.findById(id);
    }

    @Transactional
    public void joinRoom(Long roomId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Khong tim thay user: " + username));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay phong chat: " + roomId));

        // kiem tra xem user da la thanh vien chua
        Long count = entityManager.createQuery(
                "SELECT COUNT(m) FROM ChatRoomMember m WHERE m.user.id = :userId AND m.chatRoom.id = :roomId",
                Long.class)
                .setParameter("userId", user.getId())
                .setParameter("roomId", roomId)
                .getSingleResult();

        if (count == 0) {
            ChatRoomMember member = ChatRoomMember.builder()
                    .user(user)
                    .chatRoom(chatRoom)
                    .build();
            entityManager.persist(member);
            logger.info("User {} da tham gia phong: {}", username, chatRoom.getName());
        }
    }
}
