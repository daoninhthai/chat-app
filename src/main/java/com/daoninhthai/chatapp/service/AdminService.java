package com.daoninhthai.chatapp.service;

import com.daoninhthai.chatapp.dto.SystemStats;
import com.daoninhthai.chatapp.entity.ChatRoom;
import com.daoninhthai.chatapp.entity.User;
import com.daoninhthai.chatapp.entity.UserBan;
import com.daoninhthai.chatapp.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserBanRepository userBanRepository;

    @Autowired
    private ChatAttachmentRepository chatAttachmentRepository;

    @Autowired
    private OnlineUserService onlineUserService;

    public SystemStats getSystemStats() {
        LocalDateTime last24h = LocalDateTime.now().minusHours(24);

        long totalUsers = userRepository.count();
        long totalRooms = chatRoomRepository.count();
        long totalMessages = chatMessageRepository.count();
        long messagesLast24h = chatMessageRepository.countMessagesSince(last24h);
        long totalBannedUsers = userBanRepository.findAllActiveBans(LocalDateTime.now()).size();
        long totalAttachments = chatAttachmentRepository.count();

        // Count active users from online service
        long activeUsersToday = onlineUserService.getTotalOnlineUsers();

        return SystemStats.builder()
                .totalUsers(totalUsers)
                .totalRooms(totalRooms)
                .totalMessages(totalMessages)
                .activeUsersToday(activeUsersToday)
                .messagesLast24h(messagesLast24h)
                .totalBannedUsers(totalBannedUsers)
                .totalAttachments(totalAttachments)
                .build();
    }

    public List<ChatRoom> getAllRooms() {
        return chatRoomRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));

        chatRoomRepository.delete(room);
        logger.info("Room {} deleted by admin", roomId);
    }

    @Transactional
    public UserBan banUser(Long userId, Long adminId, String reason, LocalDateTime expiresAt) {
        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Check if already banned
        Optional<UserBan> existingBan = userBanRepository.findActiveBan(userId, LocalDateTime.now());
        if (existingBan.isPresent()) {
            throw new IllegalStateException("User is already banned");
        }

        UserBan ban = UserBan.builder()
                .userId(userId)
                .bannedBy(adminId)
                .reason(reason)
                .bannedAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .isActive(true)
                .build();

        UserBan saved = userBanRepository.save(ban);
        logger.info("User {} banned by admin {} - reason: {}", userId, adminId, reason);
        return saved;
    }

    @Transactional
    public void unbanUser(Long userId) {
        Optional<UserBan> activeBan = userBanRepository.findActiveBan(userId, LocalDateTime.now());

        if (activeBan.isPresent()) {
            UserBan ban = activeBan.get();
            ban.setActive(false);
            userBanRepository.save(ban);
            logger.info("User {} unbanned", userId);
        } else {
            throw new RuntimeException("No active ban found for user: " + userId);
        }
    }

    public boolean isUserBanned(Long userId) {
        Optional<UserBan> activeBan = userBanRepository.findActiveBan(userId, LocalDateTime.now());
        return activeBan.isPresent();
    }

    public List<UserBan> getActiveBans() {
        return userBanRepository.findAllActiveBans(LocalDateTime.now());
    }

    public long getRoomMessageCount(Long roomId) {
        return chatMessageRepository.countByChatRoomId(roomId);
    }
}
