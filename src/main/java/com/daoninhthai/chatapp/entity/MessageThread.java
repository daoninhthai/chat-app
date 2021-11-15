package com.daoninhthai.chatapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message_threads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageThread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_message_id", nullable = false, unique = true)
    private Long parentMessageId;

    @Column(name = "reply_count")
    private int replyCount;

    @Column(name = "last_reply_at")
    private LocalDateTime lastReplyAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (lastReplyAt == null) {
            lastReplyAt = LocalDateTime.now();
        }
    }
}
