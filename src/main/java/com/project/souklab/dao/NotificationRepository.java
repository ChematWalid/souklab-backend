package com.project.souklab.dao;

import com.project.souklab.model.Notification;
import com.project.souklab.model.NotificationType;
import com.project.souklab.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    Page<Notification> findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(User user, Pageable pageable);

    Optional<Notification> findFirstByUserAndTypeAndTargetIdAndDeletedAtIsNullOrderByCreatedAtDesc(User user, NotificationType type, String targetId);

    Optional<Notification> findByIdAndUserAndDeletedAtIsNull(String id, User user);

    long countByUserAndIsReadFalseAndDeletedAtIsNull(User user);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.updatedAt = CURRENT_TIMESTAMP WHERE n.user = :user AND n.isRead = false AND n.deletedAt IS NULL")
    int markAllAsReadForUser(@Param("user") User user);
}
