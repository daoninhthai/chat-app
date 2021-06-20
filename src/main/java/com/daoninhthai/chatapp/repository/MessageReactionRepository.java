package com.daoninhthai.chatapp.repository;

import com.daoninhthai.chatapp.entity.MessageReaction;
import com.daoninhthai.chatapp.entity.MessageReaction.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long> {

    List<MessageReaction> findByMessageId(Long messageId);

    long countByMessageIdAndReactionType(Long messageId, ReactionType reactionType);

    Optional<MessageReaction> findByMessageIdAndUserIdAndReactionType(
            Long messageId, Long userId, ReactionType reactionType);

    void deleteByMessageIdAndUserId(Long messageId, Long userId);

    void deleteByMessageIdAndUserIdAndReactionType(
            Long messageId, Long userId, ReactionType reactionType);

    @Query("SELECT r.reactionType, COUNT(r) FROM MessageReaction r " +
           "WHERE r.messageId = :messageId GROUP BY r.reactionType")
    List<Object[]> countReactionsByType(@Param("messageId") Long messageId);

    boolean existsByMessageIdAndUserIdAndReactionType(
            Long messageId, Long userId, ReactionType reactionType);
}
