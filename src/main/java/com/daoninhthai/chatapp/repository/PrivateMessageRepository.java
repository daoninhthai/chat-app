package com.daoninhthai.chatapp.repository;

import com.daoninhthai.chatapp.entity.PrivateMessage;
import com.daoninhthai.chatapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long> {

    /**
     * Tim tat ca tin nhan giua 2 user theo thu tu thoi gian
     */
    List<PrivateMessage> findBySenderAndReceiverOrderByTimestampAsc(User sender, User receiver);

    /**
     * Tim tat ca cuoc hoi thoai giua 2 user (ca 2 chieu)
     * Su dung JPQL query de lay tin nhan tu ca 2 phia
     */
    @Query("SELECT pm FROM PrivateMessage pm " +
           "WHERE (pm.sender = :user1 AND pm.receiver = :user2) " +
           "OR (pm.sender = :user2 AND pm.receiver = :user1) " +
           "ORDER BY pm.timestamp ASC")
    List<PrivateMessage> findConversation(@Param("user1") User user1,
                                           @Param("user2") User user2);

    /**
     * Dem so tin nhan chua doc cua 1 user
     */
    @Query("SELECT COUNT(pm) FROM PrivateMessage pm " +
           "WHERE pm.receiver = :receiver AND pm.read = false")
    long countUnreadByReceiver(@Param("receiver") User receiver);

    /**
     * Tim tat ca tin nhan chua doc gui den 1 user tu 1 user khac
     */
    @Query("SELECT pm FROM PrivateMessage pm " +
           "WHERE pm.sender = :sender AND pm.receiver = :receiver " +
           "AND pm.read = false " +
           "ORDER BY pm.timestamp ASC")
    List<PrivateMessage> findUnreadMessages(@Param("sender") User sender,
                                             @Param("receiver") User receiver);
}
