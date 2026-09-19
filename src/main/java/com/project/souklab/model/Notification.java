package com.project.souklab.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Convert;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @Column(columnDefinition = "TEXT")
    private String message;
    private boolean isRead = false;

    @Convert(converter = NotificationTypeConverter.class)
    @Column(length = 50)
    private NotificationType.Key type;

    private String targetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
