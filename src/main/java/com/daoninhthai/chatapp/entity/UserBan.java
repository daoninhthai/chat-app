package com.daoninhthai.chatapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_bans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "banned_by", nullable = false)
    private Long bannedBy;

    @Column(name = "banned_at")
    private LocalDateTime bannedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_active")
    private boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        if (bannedAt == null) {
            bannedAt = LocalDateTime.now();
        }
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isCurrentlyBanned() {
        return isActive && !isExpired();
    }
}
