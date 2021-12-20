package com.daoninhthai.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemStats {

    private long totalUsers;

    private long totalRooms;

    private long totalMessages;

    private long activeUsersToday;

    private long messagesLast24h;

    private long totalBannedUsers;

    private long totalAttachments;
}
