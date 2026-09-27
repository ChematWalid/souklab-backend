package com.project.souklab.dao;

import com.project.souklab.model.NotificationType;
import com.project.souklab.model.User;
import com.project.souklab.model.UserNotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserNotificationPreferenceRepository extends JpaRepository<UserNotificationPreference, String> {
    List<UserNotificationPreference> findByUser(User user);
    Optional<UserNotificationPreference> findByUserAndType(User user, NotificationType.Key type);
    void deleteByUser(User user);
}
