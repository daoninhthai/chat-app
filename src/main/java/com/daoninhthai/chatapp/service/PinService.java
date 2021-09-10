package com.daoninhthai.chatapp.service;

import com.daoninhthai.chatapp.entity.PinnedMessage;
import com.daoninhthai.chatapp.repository.PinnedMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PinService {

    private static final Logger logger = LoggerFactory.getLogger(PinService.class);

    private static final int MAX_PINS_PER_ROOM = 50;

    @Autowired
    private PinnedMessageRepository pinnedMessageRepository;

    @Transactional
    public PinnedMessage pinMessage(Long messageId, Long roomId, Long userId) {
        // Check if already pinned
        if (pinnedMessageRepository.existsByMessageIdAndRoomId(messageId, roomId)) {
            throw new IllegalStateException("Message is already pinned in this room");
        }

        // Check pin limit
        long currentPins = pinnedMessageRepository.countByRoomId(roomId);
        if (currentPins >= MAX_PINS_PER_ROOM) {
            throw new IllegalStateException("Maximum number of pinned messages reached for this room");
        }

        PinnedMessage pinned = PinnedMessage.builder()
                .messageId(messageId)
                .roomId(roomId)
                .pinnedBy(userId)
                .pinnedAt(LocalDateTime.now())
                .build();

        PinnedMessage saved = pinnedMessageRepository.save(pinned);
        logger.info("Message {} pinned in room {} by user {}", messageId, roomId, userId);
        return saved;
    }

    @Transactional
    public void unpinMessage(Long messageId, Long roomId) {
        pinnedMessageRepository.deleteByMessageIdAndRoomId(messageId, roomId);
        logger.info("Message {} unpinned from room {}", messageId, roomId);
    }

    public List<PinnedMessage> getPinnedMessages(Long roomId) {
        return pinnedMessageRepository.findByRoomIdOrderByPinnedAtDesc(roomId);
    }

    public boolean isMessagePinned(Long messageId, Long roomId) {
        return pinnedMessageRepository.existsByMessageIdAndRoomId(messageId, roomId);
    }
}
