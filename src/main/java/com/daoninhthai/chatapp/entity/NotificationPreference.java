package com.daoninhthai.chatapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "email_enabled")
    private boolean emailEnabled = true;

    @Column(name = "push_enabled")
    private boolean pushEnabled = true;

    @Column(name = "sound_enabled")
    private boolean soundEnabled = true;

    @Column(name = "mention_notifications")
    private boolean mentionNotifications = true;

    @Column(name = "direct_message_notifications")
    private boolean directMessageNotifications = true;

    @Column(name = "mute_until")
    private LocalDateTime muteUntil;

    @Column(name = "muted_rooms", columnDefinition = "TEXT")
    private String mutedRooms;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isGloballyMuted() {
        return muteUntil != null && LocalDateTime.now().isBefore(muteUntil);
    }

    public boolean isRoomMuted(Long roomId) {
        if (mutedRooms == null || mutedRooms.isEmpty()) {
            return false;
        }
        String roomIdStr = String.valueOf(roomId);
        String[] rooms = mutedRooms.split(",");
        for (String room : rooms) {
            if (room.trim().equals(roomIdStr)) {
                return true;
            }
        }
        return false;
    }
}
