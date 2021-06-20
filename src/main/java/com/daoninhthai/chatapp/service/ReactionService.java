package com.daoninhthai.chatapp.service;

import com.daoninhthai.chatapp.entity.MessageReaction;
import com.daoninhthai.chatapp.entity.MessageReaction.ReactionType;
import com.daoninhthai.chatapp.repository.MessageReactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReactionService {

    private static final Logger logger = LoggerFactory.getLogger(ReactionService.class);

    @Autowired
    private MessageReactionRepository reactionRepository;

    @Transactional
    public MessageReaction addReaction(Long messageId, Long userId, ReactionType reactionType) {
        // Check if user already reacted with same type
        Optional<MessageReaction> existing = reactionRepository
                .findByMessageIdAndUserIdAndReactionType(messageId, userId, reactionType);

        if (existing.isPresent()) {
            logger.info("User {} already reacted with {} on message {}", userId, reactionType, messageId);
            return existing.get();
        }

        MessageReaction reaction = MessageReaction.builder()
                .messageId(messageId)
                .userId(userId)
                .reactionType(reactionType)
                .createdAt(LocalDateTime.now())
                .build();

        MessageReaction saved = reactionRepository.save(reaction);
        logger.info("User {} added {} reaction to message {}", userId, reactionType, messageId);
        return saved;
    }

    @Transactional
    public void removeReaction(Long messageId, Long userId, ReactionType reactionType) {
        reactionRepository.deleteByMessageIdAndUserIdAndReactionType(messageId, userId, reactionType);
        logger.info("User {} removed {} reaction from message {}", userId, reactionType, messageId);
    }

    public List<MessageReaction> getReactions(Long messageId) {
        return reactionRepository.findByMessageId(messageId);
    }

    public Map<ReactionType, Long> getReactionCounts(Long messageId) {
        List<Object[]> results = reactionRepository.countReactionsByType(messageId);
        Map<ReactionType, Long> counts = new HashMap<>();

        for (ReactionType type : ReactionType.values()) {
            counts.put(type, 0L);
        }

        for (Object[] row : results) {
            ReactionType type = (ReactionType) row[0];
            Long count = (Long) row[1];
            counts.put(type, count);
        }

        return counts;
    }

    public boolean hasUserReacted(Long messageId, Long userId, ReactionType reactionType) {
        return reactionRepository.existsByMessageIdAndUserIdAndReactionType(
                messageId, userId, reactionType);
    }

    @Transactional
    public MessageReaction toggleReaction(Long messageId, Long userId, ReactionType reactionType) {
        Optional<MessageReaction> existing = reactionRepository
                .findByMessageIdAndUserIdAndReactionType(messageId, userId, reactionType);

        if (existing.isPresent()) {
            reactionRepository.delete(existing.get());
            logger.info("User {} toggled off {} reaction on message {}", userId, reactionType, messageId);
            return null;
        } else {
            return addReaction(messageId, userId, reactionType);
        }
    }
}
