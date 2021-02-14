package com.daoninhthai.chatapp.service;

import com.daoninhthai.chatapp.entity.PrivateMessage;
import com.daoninhthai.chatapp.entity.User;
import com.daoninhthai.chatapp.repository.PrivateMessageRepository;
import com.daoninhthai.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PrivateMessageService {

    @Autowired
    private PrivateMessageRepository privateMessageRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Gui tin nhan rieng tu sender den receiver
     */
    public PrivateMessage sendMessage(String senderUsername, Long receiverId, String content) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Khong tim thay nguoi gui: " + senderUsername));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay nguoi nhan voi id: " + receiverId));

        PrivateMessage message = PrivateMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .timestamp(LocalDateTime.now())
                .read(false)
                .build();

        return privateMessageRepository.save(message);
    }

    /**
     * Lay tat ca tin nhan trong cuoc hoi thoai giua 2 user
     */
    @Transactional(readOnly = true)
    public List<PrivateMessage> getConversation(String username, Long otherUserId) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Khong tim thay user: " + username));

        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay user voi id: " + otherUserId));

        return privateMessageRepository.findConversation(currentUser, otherUser);
    }

    /**
     * Danh dau tin nhan la da doc
     */
    public void markAsRead(Long messageId) {
        PrivateMessage message = privateMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay tin nhan voi id: " + messageId));

        message.setRead(true);
        privateMessageRepository.save(message);
    }

    /**
     * Danh dau tat ca tin nhan chua doc tu 1 user la da doc
     */
    public void markAllAsRead(String receiverUsername, Long senderUserId) {
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("Khong tim thay user: " + receiverUsername));

        User sender = userRepository.findById(senderUserId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay user voi id: " + senderUserId));

        List<PrivateMessage> unreadMessages =
                privateMessageRepository.findUnreadMessages(sender, receiver);

        unreadMessages.forEach(msg -> msg.setRead(true));
        privateMessageRepository.saveAll(unreadMessages);
    }

    /**
     * Lay so tin nhan chua doc cua 1 user
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Khong tim thay user: " + username));

        return privateMessageRepository.countUnreadByReceiver(user);
    }
}
