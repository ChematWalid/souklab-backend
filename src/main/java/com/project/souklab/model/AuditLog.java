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
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog extends BaseEntity {

    public AuditLog(AuditLogAction.Key action, String details, User user) {
        this.action = action;
        this.details = details;
        this.user = user;
    }

    @Convert(converter = AuditLogActionConverter.class)
    @Column(nullable = false, length = 50)
    private AuditLogAction.Key action;

    @Column(length = 2000)
    private String details;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "target_account_id", length = 36)
    private String targetAccountId;

    @Column(length = 80)
    private String operation;

    @Column(name = "previous_state", length = 500)
    private String previousState;

    @Column(name = "new_state", length = 500)
    private String newState;

    @Column(length = 1000)
    private String reason;

    @Column(name = "payment_id", length = 36)
    private String paymentId;

    @Column(name = "subscription_id", length = 36)
    private String subscriptionId;
}
