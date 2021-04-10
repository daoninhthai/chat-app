package com.daoninhthai.chatapp.repository;

import com.daoninhthai.chatapp.entity.ChatAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatAttachmentRepository extends JpaRepository<ChatAttachment, Long> {

    List<ChatAttachment> findByMessageId(Long messageId);

    Optional<ChatAttachment> findByFileName(String fileName);

    List<ChatAttachment> findByMessageIdIn(List<Long> messageIds);

    void deleteByFileName(String fileName);
}
