package com.daoninhthai.chatapp.repository;

import com.daoninhthai.chatapp.entity.UserBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserBanRepository extends JpaRepository<UserBan, Long> {

    List<UserBan> findByUserId(Long userId);

    @Query("SELECT b FROM UserBan b WHERE b.userId = :userId AND b.isActive = true " +
           "AND (b.expiresAt IS NULL OR b.expiresAt > :now)")
    Optional<UserBan> findActiveBan(@Param("userId") Long userId,
                                     @Param("now") LocalDateTime now);

    @Query("SELECT b FROM UserBan b WHERE b.isActive = true " +
           "AND (b.expiresAt IS NULL OR b.expiresAt > :now)")
    List<UserBan> findAllActiveBans(@Param("now") LocalDateTime now);

    boolean existsByUserIdAndIsActiveTrue(Long userId);

    List<UserBan> findByBannedByOrderByBannedAtDesc(Long adminId);
}
