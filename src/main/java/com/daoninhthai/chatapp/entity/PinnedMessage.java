package com.daoninhthai.chatapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pinned_messages",
       uniqueConstraints = @UniqueConstraint(columnNames = {"message_id", "room_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PinnedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "pinned_by", nullable = false)
    private Long pinnedBy;

    @Column(name = "pinned_at")
    private LocalDateTime pinnedAt;

    @PrePersist
    protected void onCreate() {
        if (pinnedAt == null) {
            pinnedAt = LocalDateTime.now();
        }
    }
}
