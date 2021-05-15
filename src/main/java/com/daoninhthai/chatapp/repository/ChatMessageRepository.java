package com.daoninhthai.chatapp.repository;

import com.daoninhthai.chatapp.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoomIdOrderByTimestampAsc(Long chatRoomId);

    List<ChatMessage> findTop50ByChatRoomIdOrderByTimestampDesc(Long chatRoomId);

    @Query("SELECT m FROM ChatMessage m WHERE LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND m.messageType = 'CHAT' ORDER BY m.timestamp DESC")
    Page<ChatMessage> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT m FROM ChatMessage m WHERE LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND m.chatRoom.id = :roomId AND m.messageType = 'CHAT'")
    Page<ChatMessage> searchByKeywordInRoom(@Param("keyword") String keyword,
                                            @Param("roomId") Long roomId,
                                            Pageable pageable);

    @Query("SELECT m FROM ChatMessage m WHERE LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND m.sender.id = :userId AND m.messageType = 'CHAT'")
    Page<ChatMessage> searchByKeywordAndUser(@Param("keyword") String keyword,
                                              @Param("userId") Long userId,
                                              Pageable pageable);

    @Query("SELECT m FROM ChatMessage m WHERE LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND m.timestamp BETWEEN :fromDate AND :toDate AND m.messageType = 'CHAT'")
    Page<ChatMessage> searchByKeywordAndDateRange(@Param("keyword") String keyword,
                                                   @Param("fromDate") LocalDateTime fromDate,
                                                   @Param("toDate") LocalDateTime toDate,
                                                   Pageable pageable);

    @Query("SELECT m FROM ChatMessage m WHERE LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND m.chatRoom.id = :roomId " +
           "AND m.timestamp BETWEEN :fromDate AND :toDate AND m.messageType = 'CHAT'")
    Page<ChatMessage> searchByKeywordInRoomAndDateRange(@Param("keyword") String keyword,
                                                        @Param("roomId") Long roomId,
                                                        @Param("fromDate") LocalDateTime fromDate,
                                                        @Param("toDate") LocalDateTime toDate,
                                                        Pageable pageable);

    long countBySenderId(Long senderId);
}
