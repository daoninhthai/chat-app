package com.daoninhthai.chatapp.service;

import com.daoninhthai.chatapp.dto.NotificationPreferenceDto;
import com.daoninhthai.chatapp.entity.NotificationPreference;
import com.daoninhthai.chatapp.repository.NotificationPreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationPreferenceRepository preferenceRepository;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    public NotificationPreference getOrCreatePreferences(Long userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    NotificationPreference prefs = NotificationPreference.builder()
                            .userId(userId)
                            .emailEnabled(true)
                            .pushEnabled(true)
                            .soundEnabled(true)
                            .mentionNotifications(true)
                            .directMessageNotifications(true)
                            .build();
                    return preferenceRepository.save(prefs);
                });
    }

    @Transactional
    public NotificationPreference updatePreferences(Long userId, NotificationPreferenceDto dto) {
        NotificationPreference prefs = getOrCreatePreferences(userId);

        prefs.setEmailEnabled(dto.isEmailEnabled());
        prefs.setPushEnabled(dto.isPushEnabled());
        prefs.setSoundEnabled(dto.isSoundEnabled());
        prefs.setMentionNotifications(dto.isMentionNotifications());
        prefs.setDirectMessageNotifications(dto.isDirectMessageNotifications());

        if (dto.getMuteUntil() != null) {
            prefs.setMuteUntil(dto.getMuteUntil());
        }

        if (dto.getMutedRoomIds() != null) {
            String mutedRoomsStr = dto.getMutedRoomIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            prefs.setMutedRooms(mutedRoomsStr);
        }

        NotificationPreference saved = preferenceRepository.save(prefs);
        logger.info("Notification preferences updated for user {}", userId);
        return saved;
    }

    @Transactional
    public void muteRoom(Long userId, Long roomId) {
        NotificationPreference prefs = getOrCreatePreferences(userId);

        Set<String> mutedSet = new HashSet<>();
        if (prefs.getMutedRooms() != null && !prefs.getMutedRooms().isEmpty()) {
            mutedSet.addAll(Arrays.asList(prefs.getMutedRooms().split(",")));
        }
        mutedSet.add(String.valueOf(roomId));

        prefs.setMutedRooms(String.join(",", mutedSet));
        preferenceRepository.save(prefs);
        logger.info("Room {} muted for user {}", roomId, userId);
    }

    @Transactional
    public void unmuteRoom(Long userId, Long roomId) {
        NotificationPreference prefs = getOrCreatePreferences(userId);

        if (prefs.getMutedRooms() != null && !prefs.getMutedRooms().isEmpty()) {
            Set<String> mutedSet = new HashSet<>(Arrays.asList(prefs.getMutedRooms().split(",")));
            mutedSet.remove(String.valueOf(roomId));
            prefs.setMutedRooms(mutedSet.isEmpty() ? null : String.join(",", mutedSet));
            preferenceRepository.save(prefs);
        }
        logger.info("Room {} unmuted for user {}", roomId, userId);
    }

    @Transactional
    public void muteAll(Long userId, int durationMinutes) {
        NotificationPreference prefs = getOrCreatePreferences(userId);
        prefs.setMuteUntil(LocalDateTime.now().plusMinutes(durationMinutes));
        preferenceRepository.save(prefs);
        logger.info("All notifications muted for user {} for {} minutes", userId, durationMinutes);
    }

    public boolean shouldNotify(Long userId, Long roomId) {
        NotificationPreference prefs = getOrCreatePreferences(userId);

        // Check global mute
        if (prefs.isGloballyMuted()) {
            return false;
        }

        // Check if push is enabled
        if (!prefs.isPushEnabled()) {
            return false;
        }

        // Check room-specific mute
        if (roomId != null && prefs.isRoomMuted(roomId)) {
            return false;
        }

        return true;
    }

    public void sendChatNotification(Long userId, Long roomId, String senderName, String messagePreview) {
        if (!shouldNotify(userId, roomId)) {
            return;
        }

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "CHAT_MESSAGE");
        notification.put("roomId", roomId);
        notification.put("sender", senderName);
        notification.put("preview", messagePreview.length() > 100
                ? messagePreview.substring(0, 100) + "..." : messagePreview);
        notification.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/user." + userId + ".notifications", notification);
        logger.debug("Chat notification sent to user {}", userId);
    }

    public void sendMentionNotification(Long userId, Long roomId, String senderName, String messageContent) {
        NotificationPreference prefs = getOrCreatePreferences(userId);

        if (!prefs.isMentionNotifications() || prefs.isGloballyMuted()) {
            return;
        }

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "MENTION");
        notification.put("roomId", roomId);
        notification.put("sender", senderName);
        notification.put("content", messageContent.length() > 150
                ? messageContent.substring(0, 150) + "..." : messageContent);
        notification.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/user." + userId + ".notifications", notification);
        logger.debug("Mention notification sent to user {}", userId);
    }

    public NotificationPreferenceDto toDto(NotificationPreference prefs) {
        List<Long> mutedRoomIds = new ArrayList<>();
        if (prefs.getMutedRooms() != null && !prefs.getMutedRooms().isEmpty()) {
            mutedRoomIds = Arrays.stream(prefs.getMutedRooms().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }

        return NotificationPreferenceDto.builder()
                .userId(prefs.getUserId())
                .emailEnabled(prefs.isEmailEnabled())
                .pushEnabled(prefs.isPushEnabled())
                .soundEnabled(prefs.isSoundEnabled())
                .mentionNotifications(prefs.isMentionNotifications())
                .directMessageNotifications(prefs.isDirectMessageNotifications())
                .muteUntil(prefs.getMuteUntil())
                .mutedRoomIds(mutedRoomIds)
                .build();
    }
}
