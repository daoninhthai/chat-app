package com.daoninhthai.chatapp.repository;

import com.daoninhthai.chatapp.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoomIdOrderByTimestampAsc(Long chatRoomId);

    List<ChatMessage> findTop50ByChatRoomIdOrderByTimestampDesc(Long chatRoomId);
}
