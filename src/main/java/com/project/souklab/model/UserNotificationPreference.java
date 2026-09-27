package com.project.souklab.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A user's explicit override for one typed notification category. */
@Entity
@Table(name = "user_notification_preferences",
        uniqueConstraints = @UniqueConstraint(name = "uk_notification_preference_user_type", columnNames = {"user_id", "notification_type"}))
@Getter
@Setter
@NoArgsConstructor
public class UserNotificationPreference extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Convert(converter = NotificationTypeConverter.class)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType.Key type;

    @Column(nullable = false)
    private boolean enabled = true;

    public UserNotificationPreference(User user, NotificationType.Key type, boolean enabled) {
        this.user = user;
        this.type = type;
        this.enabled = enabled;
    }
}
