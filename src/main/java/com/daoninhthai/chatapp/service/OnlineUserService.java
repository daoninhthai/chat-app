package com.daoninhthai.chatapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineUserService {

    private static final Logger logger = LoggerFactory.getLogger(OnlineUserService.class);

    // map roomId -> set of usernames dang online
    private final ConcurrentHashMap<Long, Set<String>> onlineUsersPerRoom = new ConcurrentHashMap<>();

    // map sessionId -> username (de track session)
    private final ConcurrentHashMap<String, String> sessionUserMap = new ConcurrentHashMap<>();

    // map sessionId -> roomId
    private final ConcurrentHashMap<String, Long> sessionRoomMap = new ConcurrentHashMap<>();

    public void addUser(String sessionId, String username, Long roomId) {
        sessionUserMap.put(sessionId, username);
        sessionRoomMap.put(sessionId, roomId);

        onlineUsersPerRoom
                .computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet())
                .add(username);

        logger.info("User {} online trong phong {} (session: {})", username, roomId, sessionId);
    }

    public void removeUser(String sessionId) {
        String username = sessionUserMap.remove(sessionId);
        Long roomId = sessionRoomMap.remove(sessionId);

        if (username != null && roomId != null) {
            Set<String> roomUsers = onlineUsersPerRoom.get(roomId);
            if (roomUsers != null) {
                roomUsers.remove(username);
                if (roomUsers.isEmpty()) {
                    onlineUsersPerRoom.remove(roomId);
                }
            }
            logger.info("User {} offline khoi phong {} (session: {})", username, roomId, sessionId);
        }
    }

    public Set<String> getOnlineUsers(Long roomId) {
        Set<String> users = onlineUsersPerRoom.get(roomId);
        return users != null ? Collections.unmodifiableSet(users) : Collections.emptySet();
    }

    public int getOnlineCount(Long roomId) {
        Set<String> users = onlineUsersPerRoom.get(roomId);
        return users != null ? users.size() : 0;
    }

    public boolean isUserOnline(String username, Long roomId) {
        Set<String> users = onlineUsersPerRoom.get(roomId);
        return users != null && users.contains(username);
    }
}
