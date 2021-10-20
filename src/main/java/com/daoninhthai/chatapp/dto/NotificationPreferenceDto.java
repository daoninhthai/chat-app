package com.daoninhthai.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferenceDto {

    private Long userId;

    private boolean emailEnabled;

    private boolean pushEnabled;

    private boolean soundEnabled;

    private boolean mentionNotifications;

    private boolean directMessageNotifications;

    private LocalDateTime muteUntil;

    private List<Long> mutedRoomIds;
}
