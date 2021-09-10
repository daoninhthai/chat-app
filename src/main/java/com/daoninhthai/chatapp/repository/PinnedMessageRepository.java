package com.daoninhthai.chatapp.repository;

import com.daoninhthai.chatapp.entity.PinnedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PinnedMessageRepository extends JpaRepository<PinnedMessage, Long> {

    List<PinnedMessage> findByRoomIdOrderByPinnedAtDesc(Long roomId);

    Optional<PinnedMessage> findByMessageIdAndRoomId(Long messageId, Long roomId);

    boolean existsByMessageIdAndRoomId(Long messageId, Long roomId);

    void deleteByMessageIdAndRoomId(Long messageId, Long roomId);

    long countByRoomId(Long roomId);
}
